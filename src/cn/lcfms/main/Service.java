package cn.lcfms.main;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;

import cn.lcfms.dao.BaseImpl;

public class Service {
	private String table;
	private String where="";
	private String order="";
	private String[] column;
	private String limit="";
	private Object params;
	private String sql="";
	private String primaryKey="";
	private String dbname="";

	private static HashMap<String,List<HashMap<String, Object>>> tableinfo=new HashMap<>();
	
	public Service(String table){
		this.table=table;
		String url=(String)Session.getProperties().get("url");
		url=url.substring(0, url.indexOf("?"));
		url=url.substring(url.lastIndexOf("/")+1);
		this.dbname=url;		
	}
	
	public Service(){
		String url=(String)Session.getProperties().get("url");
		url=url.substring(0, url.indexOf("?"));
		url=url.substring(url.lastIndexOf("/")+1);
		this.dbname=url;
	}
	
	public Service setTable(String table) {
		String tablePrefix=(String)Session.getProperties().getProperty("tablePrefix");
		this.table = tablePrefix+table;
		return this;
	}
	
	class KeyId {
		private int id;

		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}		
	}
	/**
	 * 执行单个字段插入
	 * @param str 字段名
	 * @return 自增的ID
	 */
	public int insert(String str){
		SqlSession session=Session.getSession(true);
		if(sql.equals("")){
			sql="insert into "+table+" (`"+str+"`) values (#{"+str+"})";	
		}	
		try {
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			KeyId keyid=new KeyId();
			map.put("keyid", keyid);
			mapper.insert(map);
			return keyid.getId();
		} catch (Exception e) {
			return -1;
		} finally{
			init();
			session.close();
		}		
	}
	/**
	 * 执行多个字段插入
	 * @param strs 字段名
	 * @return 自增的ID
	 */
	public int insert(String... strs){
		SqlSession session=Session.getSession(true);
		if(sql.equals("")){
			sql="insert into "+table+" (";			
			for(int i=0;i<strs.length;i++){
				if(i==strs.length-1){
					sql+="`"+strs[i]+"`) values (";
				}else{
					sql+="`"+strs[i]+"`,";
				}		
			}
			for(int i=0;i<strs.length;i++){
				if(i==strs.length-1){
					sql+="#{"+strs[i]+"})";
				}else{
					sql+="#{"+strs[i]+"},";
				}		
			}	
		}		
		try {
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			KeyId keyid=new KeyId();
			map.put("keyid", keyid);
			mapper.insert(map);
			return keyid.getId();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally{
			init();
			session.close();
		}	
	}
	/**
	 * 执行一个实体对象插入
	 * @param strs 字段名
	 * @return 自增的ID
	 */
	public <T> void insert(T t){
		SqlSession session=Session.getSession(true);
		TreeSet<String> tableinfo = getColumn(session);		
		Class<?> beanClass=t.getClass();
		Field[] fields=beanClass.getDeclaredFields();
		List<String> strslist=new ArrayList<>();	
		HashMap<String,Object> params=new HashMap<String,Object>();		
		for (int i=0;i<fields.length;i++) {
			fields[i].setAccessible(true);
			try {
				String key=fields[i].getName();
				Object value=fields[i].get(t);	
				if(tableinfo.contains(key) && !key.equals(primaryKey)){
					strslist.add(key);
					params.put(key, value);
				}	
			} catch (Exception e) {
				continue;
			}		
		}		
		Object[] strs=strslist.toArray();
		this.params=params;		
		if(sql.equals("")){
			sql="insert into "+table+" (";			
			for(int i=0;i<strs.length;i++){
				if(i==strs.length-1){
					sql+="`"+strs[i]+"`) values (";
				}else{
					sql+="`"+strs[i]+"`,";
				}		
			}
			for(int i=0;i<strs.length;i++){
				if(i==strs.length-1){
					sql+="#{"+strs[i]+"})";
				}else{
					sql+="#{"+strs[i]+"},";
				}		
			}	
		}		
		try {
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			KeyId keyid=new KeyId();
			map.put("keyid", keyid);
			mapper.insert(map);
			int id=keyid.getId();
			String setprimaryKey = "set"+primaryKey.replaceFirst(primaryKey.substring(0, 1),primaryKey.substring(0, 1).toUpperCase());
			Method method=beanClass.getMethod(setprimaryKey, int.class);
			method.invoke(t, id);			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			init();
			session.close();
		}	
	}
	
	private TreeSet<String> getColumn(SqlSession session){
		List<HashMap<String, Object>> list;
		if(Service.tableinfo.get(table)==null){
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", "select COLUMN_NAME,COLUMN_KEY from information_schema.columns where TABLE_NAME='"+table+"' and TABLE_SCHEMA='"+dbname+"'");
			list= mapper.selectList(map);
			Service.tableinfo.put(table, list);
		}else{
			list =Service.tableinfo.get(table);
		}
		TreeSet<String> set=new TreeSet<>();
		for(int i=0;i<list.size();i++){
			set.add((String)list.get(i).get("COLUMN_NAME"));
			if(!list.get(i).get("COLUMN_KEY").equals("")){
				this.primaryKey=(String)list.get(i).get("COLUMN_NAME");
			}
		}
		return set;
	}
	
	/**
	 * 执行删除行
	 */
	public boolean delete(){
		SqlSession session=Session.getSession(true);
		if(sql.equals("")){
			sql="delete from "+table;
		}
		try {
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			mapper.delete(map);
			return true;
		} catch (Exception e) {
			return false;
		}finally{
			init();
			session.close();
		}	
	}
	/**
	 * 执行删除行
	 */
	public boolean deleteById(int id){
		SqlSession session=Session.getSession(true);
		this.getColumn(session);
		String key=this.primaryKey;		
		sql="delete from "+table+" where "+key+"=#{"+key+"}";
		this.setData(id);
		try {		
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			mapper.delete(map);
			return true;
		} catch (Exception e) {
			return false;
		}finally{
			init();
			session.close();
		}	
	}
	/**
	 * 修改单个字段
	 * @param s 字段名
	 */
	public boolean update(String s){
		SqlSession session=Session.getSession(true);
		if(sql.equals("")){
			if(s.indexOf("=")!=-1){
				sql="update "+table+" set "+s;	
			}else{
				sql="update "+table+" set `"+s+"`="+"#{"+s+"}";	
			}	
		}
		try {
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			mapper.update(map);
			return true;
		} catch (Exception e) {
			return false;
		} finally{
			init();
			session.close();
		}		
	}
	/**
	 * 修改多个字段
	 * @param strs 字段名
	 */
	public boolean update(String...strs){
		SqlSession session=Session.getSession(true);
		if(sql.equals("")){
			sql="update "+table+" set ";	
			for(int i=0;i<strs.length;i++){
				if(i==0){
					if(strs[i].indexOf("=")!=-1){
						sql+=strs[i];	
					}else{
						sql+="`"+strs[i]+"`=#{"+strs[i]+"}";	
					}							
				}else{	
					if(strs[i].indexOf("=")!=-1){
						sql+=","+strs[i];		
					}else{
						sql+=",`"+strs[i]+"`=#{"+strs[i]+"}";
					}						
				}
			}		
		}		
		try {
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			mapper.update(map);
			return true;
		} catch (Exception e) {
			return false;
		} finally{
			init();
			session.close();
		}	
	}
	/**
	 * 执行一个实体对象的更新
	 * @param strs 字段名
	 */
	public <T> boolean update(T t){
		SqlSession session=Session.getSession(true);
		TreeSet<String> tableinfo = getColumn(session);		
		Class<?> beanClass=t.getClass();
		Field[] fields=beanClass.getDeclaredFields();
		List<String> strslist=new ArrayList<>();	
		HashMap<String,Object> params=new HashMap<String,Object>();	
		for (int i=0;i<fields.length;i++) {
			fields[i].setAccessible(true);
			try {
				String key=fields[i].getName();
				Object value=fields[i].get(t);	
				if(tableinfo.contains(key) && !key.equals(primaryKey)){
					strslist.add(key);
					params.put(key, value);
				}else if(key.equals(primaryKey)){
					params.put(key, value);
				}
			} catch (Exception e) {
				continue;
			}		
		}		
		Object[] strs=strslist.toArray();
		this.params=params;		
		sql="update "+table+" set ";	
		for(int i=0;i<strs.length;i++){
			if(i==0){
				sql+="`"+strs[i]+"`=#{"+strs[i]+"}";						
			}else{	
				sql+=",`"+strs[i]+"`=#{"+strs[i]+"}";				
			}
		}
		sql+=" where "+primaryKey+"=#{"+primaryKey+"}";
		try {
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			mapper.update(map);	
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally{
			init();
			session.close();
		}	
	}
	/**
	 * 查询
	 * @return list集合
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> selectList(){
		SqlSession session=Session.getSession(true);
		if(sql.equals("")){
			sql="select ";
			if(column==null){
				sql+="*";
			}else{
				for(int i=0;i<column.length;i++){
					if(i==0){
						sql+=column[i];
					}else{
						sql+=","+column[i];
					}			
				}
			}	
			sql+=" from "+table;
		}		
		List<HashMap<String, Object>> list=new ArrayList<HashMap<String,Object>>();
		try {
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			list=mapper.selectList(map);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			init();
			session.close();
		}			
		return list;
	}	
	
	/**
	 * 查询
	 * @return map集合
	 * @throws Exception
	 */
	public HashMap<String, Object> selectMap(){
		SqlSession session=Session.getSession(true);
		if(sql.equals("")){
			sql="select ";
			if(column==null){
				sql+="*";
			}else{
				for(int i=0;i<column.length;i++){
					if(i==0){
						sql+=column[i];
					}else{
						sql+=","+column[i];
					}			
				}
			}	
			sql+=" from "+table;
		}		
		HashMap<String, Object> data=new HashMap<String,Object>();
		try {
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			List<HashMap<String, Object>> list = mapper.selectList(map);
			if(list.size()>0){
				data=list.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			init();
			session.close();
		}			
		return data;
	}	
	/**
	 * 查询
	 * @return 实体对象
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T> T selectOne(Class<?> cl){
		SqlSession session=Session.getSession(true);
		if(sql.equals("")){
			sql="select ";
			if(column==null){
				sql+="*";
			}else{
				for(int i=0;i<column.length;i++){
					if(i==0){
						sql+=column[i];
					}else{
						sql+=","+column[i];
					}			
				}
			}	
			sql+=" from "+table;
		}	
		T obj=null;
		try {
			obj = (T) cl.newInstance();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}		
		try {
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			List<HashMap<String, Object>> list = mapper.selectList(map);
			if(list.size()>0){
				HashMap<String, Object> data=list.get(0);
				TreeSet<String> tableinfo = getColumn(session);
				Field[] fields = cl.getDeclaredFields();
				for (int i=0;i<fields.length;i++) {
					fields[i].setAccessible(true);			
					String key=fields[i].getName();			
					if(tableinfo.contains(key)){
						String setKey = "set"+key.replaceFirst(key.substring(0, 1),key.substring(0, 1).toUpperCase());
						try{	
							Method method=cl.getMethod(setKey,fields[i].getType());							
							method.invoke(obj, data.get(key));								
						}catch(NoSuchMethodException e){
							continue;
						}catch (IllegalArgumentException e) {
							String str="属性"+fields[i].getName()+"的类型是"+fields[i].getType()+",但数据库查出来的类型是"+data.get(key).getClass()+",两种类型不能匹配,所以这个属性映射失败";
							System.out.println(str);
						}	
					}	
					
				}				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			init();
			session.close();
		}			
		return obj;
	}	
	
	/**
	 * 查询数量
	 * @return 实体对象
	 * @throws Exception
	 */
	public int selectCount(){
		SqlSession session=Session.getSession(true);
		if(sql.equals("")){
			sql="select count(1) as count from "+table;
		}else{
			sql="select count(1) as count "+sql.substring(sql.indexOf("from"));
		}		
		List<HashMap<String, Object>> list=new ArrayList<>();
		try {
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			list = mapper.selectList(map);		
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			init();
			session.close();
		}		
		Long l = (Long) list.get(0).get("count");
		return l.intValue();
	}
	/**
	 * 
	 * @param pagerow 每页显示多少行
	 * @param pagenum 分页最多显示多少个页码,必须是基数,一般写5或者7个
	 */
	public HashMap<String, Object> selectPage(int pagerow,int pagenum,HttpServletRequest request){
		SqlSession session=Session.getSession(true);
		int page=1;
		try {
			page = Integer.valueOf(request.getParameter("page"));
		} catch (Exception e) {
			page = 1;
		}
		this.limit((page-1)*pagerow, pagerow);	
		if(sql.equals("")){
			sql="select ";
			if(column==null){
				sql+="*";
			}else{
				for(int i=0;i<column.length;i++){
					if(i==0){
						sql+=column[i];
					}else{
						sql+=","+column[i];
					}			
				}
			}	
			sql+=" from "+table;
		}		
		List<HashMap<String, Object>> list=new ArrayList<HashMap<String,Object>>();
		try {
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			list=mapper.selectList(map);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			session.close();
		}			
		session=Session.getSession(true);
		if(sql.indexOf("order by")!=-1){
			sql="select count(1) as count "+sql.substring(sql.indexOf("from"),sql.indexOf("order by"));
		}else{
			sql="select count(1) as count "+sql.substring(sql.indexOf("from"),sql.indexOf("limit"));
		}	
		int count=0;
		try {			
			this.limit="";
			this.order="";
			changeSQL();
			BaseImpl mapper = session.getMapper(BaseImpl.class);	
			HashMap<String,Object> map=new HashMap<>();
			map.put("sql", sql);
			setParamData(map);
			Long l = (Long) mapper.selectList(map).get(0).get("count");	
			count=l.intValue();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			init();
			session.close();
		}	
		HashMap<String, Object> result=new HashMap<String, Object>();
		result.put("total", count);
		result.put("data", list);
		result.put("nowPage", page);
		Page p=new Page(count, pagerow, pagenum, request);
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("first", p.first());//首页
		map.put("end", p.end());//末页
		map.put("pagelist", p.pagelist());//页码列表
		map.put("count", p.count());//统计信息
		map.put("nowpage", p.nowpage());//当前页信息	
		result.put("page", map);
		return result;
	}
	
	private void setParamData(HashMap<String,Object> map){
		String regex="#\\{\\w*?\\}";
		Pattern pattern=Pattern.compile(regex);
		Matcher matcher = pattern.matcher(sql);
		int i=0;
		while(matcher.find()){
			String p=matcher.group();
			p=p.substring(2, p.length()-1);
			if(params instanceof Object[]){
				Object[] paramArr=(Object[])params;
				map.put(p, paramArr[i]);			
			}else if(params instanceof List){
				List<?> list=(List<?>) params;
				map.put(p, list.get(i));
			}else if(params instanceof Map){
				Map<?,?> m=(Map<?,?>) params;
				map.put(p, m.get(p));
			}
			i++;
		}
	}
	
	
	public Service where(String where){
		this.where+=" where "+where;
		return this;
	}
	
	private void init(){
		this.where="";
		this.order="";
		this.column=null;
		this.limit="";
		this.params=null;
		this.sql="";
	}
	
	private void changeSQL(){
		String w="";
		String o="";
		String l="";
		if(sql.indexOf("where")!=-1){
			w=sql.substring(sql.indexOf("where"),sql.length());
		}
		if(sql.indexOf("order by")!=-1){
			o=sql.substring(sql.indexOf("order by"),sql.length());
			if(w.indexOf("order by")!=-1){
				w=w.substring(0, w.indexOf("order by"));
			}
		}
		if(sql.indexOf("limit")!=-1){
			l=sql.substring(sql.indexOf("limit"),sql.length());
			if(o.indexOf("limit")!=-1){
				o=o.substring(0, o.indexOf("limit"));
			}	
			if(w.indexOf("limit")!=-1){
				w=w.substring(0, w.indexOf("limit"));
			}		
		}		
		if(sql.indexOf("where")!=-1){
			sql=sql.substring(0, sql.indexOf("where"));
		}
		if(sql.indexOf("order by")!=-1){
			sql=sql.substring(0, sql.indexOf("order by"));
		}
		if(sql.indexOf("limit")!=-1){
			sql=sql.substring(0, sql.indexOf("limit"));
		}
		if(where.equals("")){
			where=w;
		}
		if(order.equals("")){
			order=o;
		}
		if(limit.equals("")){
			limit=l;
		}
		sql=sql.trim();
		if(!where.trim().equals("")){
			where=" "+where;
		}
		if(!order.trim().equals("")){
			order=" "+order;
		}
		if(!limit.trim().equals("")){
			limit=" "+limit;
		}	
		sql=sql+where+order+limit;	
	}
	/**
	 * 可以传可变参数,数组,map,list
	 * @param obj
	 * @return
	 */
	public Service setData(Object... obj){
		this.params=obj;
		return this;
	}
	/**
	 * 可以传可变参数,数组,map,list
	 * @param obj
	 * @return
	 */
	public Service setData(Map<String, Object> map){
		this.params=map;
		return this;
	}
	/**
	 * 可以传可变参数,数组,map,list
	 * @param obj
	 * @return
	 */
	public Service setData(List<Object> list){
		this.params=list;
		return this;
	}
	public Service column(String... s){
		this.column=s;
		return this;
	}
	public Service column(String s){
		this.column=new String[1];
		this.column[0]=s;
		return this;
	}
	public Service sql(String s){
		this.sql=s;
		return this;
	}
	public Service order(String order){
		this.order+=" order by "+order;
		return this;
	}
	public Service limit(int begin,int end){
		this.limit+=" limit "+begin+","+end;
		return this;
	}
	
	
}
