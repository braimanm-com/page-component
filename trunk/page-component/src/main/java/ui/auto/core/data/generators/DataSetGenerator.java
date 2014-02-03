/*
Copyright 2010-2012 Michael Braiman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package ui.auto.core.data.generators;

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ui.auto.core.context.PageComponentContext;
import ui.auto.core.data.DataAliases;
import ui.auto.core.data.DataPersistence;
import ui.auto.core.data.generators.AddressGenerator.Address;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class DataSetGenerator {
	private AddressGenerator addrGen=null;
	private Address address = null;
	private HumanNameGenerator nameGen=null;
	private WordGenerator wordGen=null;
	private static String timeStamp=new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(Calendar.getInstance().getTime());
	private static String filePath="generated-data/"+timeStamp; 
	private static DataSetGenerator instance=null;
	private int recursionLevel=2;
	private int levelCounter=0;
	int containerLevelCounter=0;
	private DataAliases aliases;
	
	protected DataSetGenerator(){	
	}
	
	public int getRecursionLevel() {
		return recursionLevel;
	}
	
	public void setRecursionLevel(int recursionLevel) {
		this.recursionLevel = recursionLevel;
	}

	public static DataSetGenerator getInstance(){
		if (instance==null){
			instance = new DataSetGenerator();
		}
		return instance;
	}
	

	String generate(Field field){
		if (!field.isAnnotationPresent(Data.class))
			return field.getName();
		
		String value=field.getAnnotation(Data.class).value();
		GeneratorType type=field.getAnnotation(Data.class).type();
		String init=field.getAnnotation(Data.class).init();
		String alias =field.getAnnotation(Data.class).alias().replace("${","").replace("}","");
		String returnValue=null;
		if (aliases!=null && aliases.containsKey(alias) && value.isEmpty()) 
			return "${" + alias + "}";
		if (value.isEmpty() && type==null)
			value=field.getName();
		switch (type){
			case FIXED_VALUE:
				returnValue=value;
				break;
			case ADDRESS:
				if (addrGen==null) addrGen=new AddressGenerator();
				if (!init.isEmpty() || address==null) {
					address=addrGen.generateAddress();
				}
				returnValue=address.toString(value);
				break;
			case CUSTOM_LIST:
				CustomListGenerator genList=new CustomListGenerator(value.split(","));
				returnValue=genList.getValue();
				break;
			case ALPHANUMERIC:
				AlphaNumericGenerator genAlpha=new AlphaNumericGenerator();
				returnValue= genAlpha.generate(value);
				break;
			case DATE:
				String[] args=init.split("\\|");
				try {
					DateGenerator dateGen=new DateGenerator(args[0],args[1],args[2]);
					if (value.isEmpty()){
						returnValue=dateGen.getDate();
					} else {
						returnValue=dateGen.getDate(value);
					}
					
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				break;
			case HUMAN_NAMES:
				if (nameGen==null) nameGen=new HumanNameGenerator();
				returnValue=nameGen.getFullName(value);
				break;
			case WORD:
				if (wordGen==null) wordGen=new WordGenerator();
				returnValue=wordGen.generate(value);
				break;
			case NUMBER:
				String[] limits=init.split(",");
				NumberGenerator numGen=new NumberGenerator(limits[0],limits[1],value);
				returnValue=numGen.getNum();
				break;
			case FILE2LIST:
				File2ListGenerator fileList=new File2ListGenerator(value);
				returnValue=fileList.getValue();
				break;
		}
		
		if (!alias.isEmpty()) {
			if (aliases==null){
				aliases=PageComponentContext.getGlobalAliases();
			}
			if (aliases.containsKey(alias) && !aliases.get(alias).equals(value))
				throw new RuntimeException("Alias '" + alias + "' is already in use!" );
			aliases.put(alias, returnValue);
			returnValue="${" + alias +"}";
		}
		return returnValue;
	}
	
	FieldTypes findBaseClass(Class<?> clz){
		if (clz.isArray()){ 
			return FieldTypes.ARRAY;
		}
		Class<?> superClz=clz;
		do {
			clz=superClz;
			superClz=clz.getSuperclass();
			
		} while (superClz!=null && superClz!=Object.class && superClz!=Number.class);
		String typeName=clz.getSimpleName().toUpperCase();

		if (typeName.equals("INT"))
				typeName="INTEGER";
		if (typeName.equals("CHAR"))
				typeName="CHARACTER";
		FieldTypes fieldType=FieldTypes.UNKNOWN_OBJECT;
		try{
			fieldType=FieldTypes.valueOf(typeName);
		} catch (Exception e){
			//Do nothing
		}
		if (fieldType.equals(FieldTypes.UNKNOWN_OBJECT) && clz.isInterface())
			return FieldTypes.IGNORED;
		return fieldType;
	}
	
	public void generateData(Object obj,boolean root) throws Exception {
		if (obj==null) return;
		Field[] fields=obj.getClass().getDeclaredFields();
		for (Field field:fields){
			if (root) 
				containerLevelCounter=0;
			field.setAccessible(true);
			if (field.isAnnotationPresent(XStreamOmitField.class)) 
				continue;
			String dataValue=generate(field);
			FieldTypes fieldType=findBaseClass(field.getType());
			if (obj.getClass().equals(field.getType())) //This is to avoid nested object recursion
				levelCounter++;
			if (levelCounter<=recursionLevel){
				Object oValue=fieldType.instantiate(null,field,dataValue);
				field.set(obj, oValue);
			} else {
				levelCounter=0;
			}
		}
		if (root){
			if (DataPersistence.class.isAssignableFrom(obj.getClass()) && aliases!=null){
				DataPersistence dataPersist= (DataPersistence) obj;
				for (String key:aliases.keySet()){
					dataPersist.setDataAlais(key,(String) aliases.get(key));
				}
				
			}
		}
		
	}
	
	public String generateDataSet(Object dataSet){
		if (DataPersistence.class.isAssignableFrom(dataSet.getClass())) {
			File file=new File(filePath);
			file.mkdirs();
			try {
				generateData(dataSet,true);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			String fileName=null;
			file=null;
			int i=0;
			String suffix="";
			do {
				fileName=filePath + "/" + dataSet.getClass().getSimpleName() + "_" + Thread.currentThread().getId() + suffix + ".xml";
				file=new File(fileName);
				suffix="_" + ++i;
			} while (file.exists());
			((DataPersistence) dataSet).toFile(fileName);
			return (fileName);
		} 
		throw new RuntimeException("Argument is not DataPersistence type!");
	}
	
}
