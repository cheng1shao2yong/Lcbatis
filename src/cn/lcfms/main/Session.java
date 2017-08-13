package cn.lcfms.main;

import java.lang.reflect.Constructor;
import java.util.Properties;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class Session{
	private static Session session;
	private Properties properties;
	private SqlSessionFactory sqlSessionFactory;
	private Session(){}
	
	public static void init(Properties properties,SqlSessionFactory sqlSessionFactory){
		if(Session.session==null){
			Session.session=new Session();					
		}
		if(properties!=null){
			Session.session.properties=properties;
		}
		if(sqlSessionFactory!=null){
			Session.session.sqlSessionFactory=sqlSessionFactory;
			Configuration configuration = sqlSessionFactory.getConfiguration();
			try {
				configuration.addMapper(Class.forName("cn.lcfms.dao.BaseImpl"));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}	
		}		
	}  
	
	public static Properties getProperties(){
		return session.properties;
	}
	
	public static SqlSession getSession(){	
		SqlSession s=session.sqlSessionFactory.openSession();
		return s;
	}
	
	public static SqlSession getSession(boolean t){
		SqlSession s;
		if(t){
			//获取自动提交的session
			s=session.sqlSessionFactory.openSession(true);
		}else{
			s=session.sqlSessionFactory.openSession();
		}	
		return s;
	}
	
	public static Service getService(String table){		
		String servicePackage=session.properties.getProperty("servicePackage");
		String tablePrefix=session.properties.getProperty("tablePrefix");
		if(servicePackage==null){
			servicePackage="";
		}
		if(tablePrefix==null){
			tablePrefix="";
		}
		String endName="";
		if(servicePackage.indexOf("{table}")!=-1){
			endName=servicePackage.substring(servicePackage.indexOf("{table}")+7);
			servicePackage=servicePackage.substring(0, servicePackage.lastIndexOf("."));
		}
		Service service;
		try {
			String tableUpper = table.replaceFirst(table.substring(0, 1),table.substring(0, 1).toUpperCase())+endName;// 将首字母改为大写
			Class<?> tablename = Class.forName(servicePackage+ "."+tableUpper);
			Constructor<?> cons=tablename.getConstructor();
			service=(Service) cons.newInstance();
			service.setTable(table);
		} catch (Exception e) {
			e.printStackTrace();
			table = table.replaceFirst(table.substring(0, 1),table.substring(0, 1).toLowerCase());// 将首字母改为小写
			service=new Service(tablePrefix+table);
		}		
		return service;
	}
	
	public static Service getService(){	
		Service service=new Service();
		return service;
	}
	
}
