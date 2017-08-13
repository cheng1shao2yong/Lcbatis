package cn.lcfms.service;


import org.apache.ibatis.session.SqlSession;

import cn.lcfms.main.Service;
import cn.lcfms.main.Session;

public class DemoService extends Service{	
	//在Service层中可以直接使用sqlsession对象
	public void test1(){
		SqlSession session=Session.getSession(true);
		/*在此执行mybatis原生的session操作*/	 
	}
	
	public void test2(){		
		//可以直接使用this来调用父方法了
		int lastId=this.setData("abc",1,2).insert("s","i","cid");
		System.out.println(lastId);
	}
	
}
