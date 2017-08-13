## Lcbatis 是神马！
**lcbatis**是mybatis的增强工具，在mybaits的基础上真正实现零配置扩展，对于一般的sql语句可以告别xml了，简直爽到爆，有木有，交流群：348455534。
#### <广告>顺便求一份高薪滴工作（要高薪哟<*_*>）</广告>

## 用法（零配置，超简单）
1. 把根目录那个Lcbatis.jar包下载下来导入到项目里面就OK了。
**（重要：因为我小小修改了mybatis源代码的几个地方，所以Lcbatis.jar的导入的优先级必须要高于mybaits.jar，如果你没改过包名，默认是优先的）**
1. 获取Service对象
		Service service = Session.getService("表名");
		//执行Service的方法，比如下面这句是根据主键id删除行
		service.deleteById(27);

## 更多教程（可以将源代码下载下来看test包里的案例）
	public class Test {
	/*
	 * 1、当SqlSessionFactory对象被创建时会自动装载到Session下,所以如果没有装载,则可以运行下面static内代码来装载
	 * 2、通常情况下我们都使用spring注入SqlSessionFactoryBean对象,这时可以省略static内的代码
	 * 3、当你自己初始化过SqlSessionFactory对象,也可以省略static内的代码
	 */
	static{
		//你的mybatis配置文件的位置
		String resource = "mybatis-config.xml";
		try {
			InputStream inputStream = Resources.getResourceAsStream(resource);
			new SqlSessionFactoryBuilder().build(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/* 执行添加操作,参数是按顺序传递
	 * sql:insert into laocheng_demo (`s`,`i`,`cid`) values (#{s},#{i},#{cid})
	 * param:abc(String), 1(Integer), 2(Integer)
	 * return:自增的id
	 */
	public void t1(){		
		Service service = Session.getService("demo");			
		int lastId=service.setData("abc",1,2).insert("s","i","cid");
		System.out.println(lastId);
	}
	/* 执行添加操作,参数是按map的key进行匹配
	 * sql:insert into laocheng_demo (`s`,`i`,`cid`) values (#{s},#{i},#{cid})
	 * param:abcde(String), 1(Integer), 2(Integer)
	 * return:自增的id
	 */
	public void t2(){		
		Service service = Session.getService("demo");	
		HashMap<String, Object> map=new HashMap<>();
		map.put("cid", 2);
		map.put("i", 1);
		map.put("s", "abcde");
		int lastId=service.setData(map).insert("s","i","cid");
		System.out.println(lastId);
	}
	/* 添加一个实体对象到数据库,参数自动匹配“表的字段名”与“实体的属性名”,匹配成功的属性将提交到数据库
	 * sql:insert into laocheng_demo (`i`,`s`,`cid`) values (#{i},#{s},#{cid})
	 * param:5(Integer), aaa(String), 45(Integer)
	 * 自增id将自动映射到实体属性中(必须名字相同,类型也相同)
	 */
	public void t3(){		
		Service service = Session.getService("demo");	
		Demo demo=new Demo();
		demo.setI(5);	
		demo.setS("aaa");
		demo.setCid(45);
		service.insert(demo);
		Vardump.print(demo);//打印查看结果
	}
	/* 执行删除操作
	 * sql:delete from laocheng_demo  where tid=#{tid}
	 * param:1(Integer)
	 */
	public void t4(){		
		Service service = Session.getService("demo");	
		//where("tid=1")也可以,但如果1是变量的话不建议使用字符串拼接,不安全容易被注入
		service.where("`tid`=#{tid}").setData(1).delete();
	}
	/* 根据主键id执行删除操作
	 * sql:delete from laocheng_demo where tid=#{tid}
	 * param:27(Integer)
	 */
	public void t5(){		
		Service service = Session.getService("demo");
		//直接传主键id的值,程序会自动获取主键名并删除行
		service.deleteById(27);
	}
	/* 执行修改操作,参数会按setData的顺序读取,条件参数要写在结尾,当然如果你用参数改用hashmap,可以自动匹配key的值,不需要考虑顺序
	 * sql:update laocheng_demo set `s`=#{s},`i`=#{i},`cid`=#{cid}  where tid=#{tid}
	 * param:abc(String), 123(Integer), 456(Integer), 50(Integer)
	 */
	public void t6(){		
		Service service = Session.getService("demo");
		//直接传主键id的值,程序会自动获取主键名并删除行
		service.where("tid=#{tid}").setData("abc",123,456,50).update("s","i","cid");
	}
	/* 根据实体对象保存修改,系统会根据属性自动匹配主键以及被修改的字段名
	 * sql:update laocheng_demo set `i`=#{i},`s`=#{s},`cid`=#{cid} where tid=#{tid}
	 * param:5(Integer), aaa(String), 45(Integer), 50(Integer)
	 */	
	public void t7(){		
		Service service = Session.getService("demo");
		Demo demo=new Demo();		
		demo.setTid(50);//主键
		demo.setI(5);	
		demo.setS("aaa");
		demo.setCid(45);
		service.update(demo);
	}
	/* 拼装sql查询,除了执行必须排在最后,其他顺序不限制
	 * sql:select cid,s from laocheng_demo  where tid>#{tid} and s=#{s}  order by tid desc  limit 0,10
	 * param:50(Integer), afdfsd(String)
	 */
	public void t8(){	
		Service service = Session.getService("demo");
		Object o=service.where("tid>#{tid} and s=#{s}")//设置条件
			   .column("cid","s")//设置要查询的字段
			   .limit(0, 10)//取行数,仅支持mysql
			   .order("tid desc")//排序
		       .setData(50,"afdfsd")//传递参数	
		       .selectList();//返回一个list集合,适用于多条数据
			 //.selectMap();//返回一个map,适用于一条数据
			 //.selectOne(Demo.class);//返回一个实体对象
			 //.selectCount();//返回行数	
		Vardump.print(o);//打印查看结果
	}
	/* 对于复杂的sql,多表查询,可以用sql()方法,但还是建议老老实实写xml,不然你不如用hibernate
	 * sql:select * from laocheng_demo a,laocheng_demochild b  where a.cid=b.cid  order by a.tid desc  limit 0,10
	 * param:50(Integer), afdfsd(String)
	 */
	public void t9(){	
		Service service = Session.getService("demo");
		List<HashMap<String, Object>> list = service
			   .sql("select * from laocheng_demo a,laocheng_demochild b")
		       .where("a.cid=b.cid")//设置条件
			   .limit(0, 10)//取行数,仅支持mysql
			   .order("a.tid desc")//排序
		       .setData(50,"afdfsd")//传递参数	
		       .selectList();//返回一个list集合,适用于多条数据	
		Vardump.print(list);//打印查看结果
	}
	/*
	 * 可以在数据源中配置下面两个属性
	 * <property name="servicePackage" value="cn.lcfms.service.{table}Service"/>
	 * <property name="tablePrefix" value="laocheng_"/>
	 * servicePackage为你service所在的包为cn.lcfms.service以及service的命名规范为“表名+Service”
	 * 
	 * tablePrefix为表前缀
	 * 没配置了tablePrefix需要写表的完整名字Session.getService("laocheng_demo"),laocheng_demo是表名
	 * 配置了tablePrefix可以简化为Session.getService("demo")
	 * 
	 * 这样你就可以像下面这样直接获取service对象
	 */
	@Test
	public void t10(){	
		DemoService service = (DemoService) Session.getService("demo");
		service.test1();
		service.test2();
	}
	
    /*
     * 执行分页查询
     * 目前只支持mysql数据库,如果是多表查询,可以使用sql("sql语句").selectPage(10,7,request);
     * 可以单独使用Page p=new Page(总记录数, 每页行数, 页码数, request)来获取分页的页码
     * p.first()//首页
	 * p.end()//末页
	 * p.pagelist()//页码列表
	 * p.count()//统计信息
	 * p.nowpage()//当前页信息	
     * 
     */
	public void t11(HttpServletRequest request){
		Map<String, Object> map=Session.getService("item")
				.column("itemId")
				.where("itemId>#{itemId}")
				.setData(10)
				.order("itemId desc")
				.selectPage(10,7,request);		
		Vardump.print(map);//推荐用来在后台查看各种类型的数据		
	}
	
	}

