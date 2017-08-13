package cn.lcfms.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;


public interface BaseImpl {
	@Select("${sql}")
	public List<HashMap<String,Object>> selectList(Map<?,?> map);
	
	@Delete("${sql}")
	public void delete(Map<?,?> map);
	
	@Update("${sql}")
	public void update(Map<?,?> map);
	
	@Insert("${sql}")
	@SelectKey(statement = "select last_insert_id()", keyProperty = "keyid.id", before = false, resultType = int.class)
	public void insert(Map<?,?> map);
}
