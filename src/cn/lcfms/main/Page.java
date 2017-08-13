/*
 * 分页类
 */
package cn.lcfms.main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class Page {
	private int total_rows;// 总记录数
	private int total_page;// 总页数
	private int onepage_rows;// 每页显示行数
	private int self_page;// 当前页
	private String url;// URL地址
	private int page_rows;// 每页页码数量,必须为奇数，否则出错
	private int start_id;// 当前页起始ID
	private int end_id;// 当前页结束ID
	private HashMap<String, String> desc;
    private HttpServletRequest request;
    /**
     * 
     * @param total 总条数
     * @param rows 每页显示行数
     * @param page_rows 页码数量
     * @param request
     */
	public Page(int total, int rows, int page_rows, HttpServletRequest request){
		this.request=request;
		this.total_rows = total;// 总条数
		this.onepage_rows = rows;// 每页显示行数
		this.page_rows = page_rows;// 页码数量
		this.total_page = (this.total_rows % this.onepage_rows==0)? (this.total_rows / this.onepage_rows):(int)(this.total_rows / this.onepage_rows)+1;// 总页数
		try {
			this.self_page = Integer.valueOf(request.getParameter("page"));
		} catch (Exception e) {
			this.self_page = 1;
		}
		if (this.self_page > this.total_page || this.self_page <= 0) {
			this.self_page = 6;
		}
		this.start_id = (this.self_page - 1) * this.onepage_rows + 1;// 起始ID
		this.end_id = (this.total_rows < this.self_page * this.onepage_rows) ? this.total_rows
				: this.self_page * this.onepage_rows;// 结束ID
		this.url = this.requestUrl();// 配置URL地址,where为传递其他参数
		this.desc = this.fdesc();
	}

	/*
	 * 重写URL地址
	 */
	private String requestUrl() {
		String url = null;
		if (this.request.getServerPort() != 80) {
			url = "http://" + this.request.getServerName() + ":"
					+ this.request.getServerPort() + this.request.getRequestURI();
		} else {
			url = "http://" + this.request.getServerName() + this.request.getRequestURI();
		}
		url += "?";
		Map<String, String[]> map =request.getParameterMap();
		Set<Entry<String, String[]>> set=map.entrySet();
		Iterator<Entry<String, String[]>> itor=set.iterator();
		int i=0;
		while (itor.hasNext()) {
			Entry<String, String[]> en=itor.next();
			String[] val=en.getValue();
			if(en.getKey().equals("page")){
				continue;
			}
			if(i==0){
				url+=en.getKey()+"="+val[0];				
			}else{
				url+="&"+en.getKey()+"="+val[0];		
			}
			i++;
		}
		if(url.endsWith("?")){
			url += "page=";			
		}else{
			url += "&page=";
		}		
		return url;
	}
	/*
	 * 配置分页文字描述
	 */
	public HashMap<String, String> fdesc() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("first", "首页");
		map.put("next", "下一页");
		map.put("pre", "上一页");
		map.put("end", "末页");
		map.put("unit", "条");
		return map;
	}
	/*
	 * 上一页
	 */
	public String pre() {
		return this.self_page > 1 ?this.url+ (this.self_page - 1): "";
	}

	/*
	 * 下一页
	 */
	public String next() {
		return this.self_page < this.total_page ? this.url+ (this.self_page + 1): "";
	}

	/*
	 * 首页
	 */
	public String first(){
		return this.url + "1";
	}

	/*
	 * 末页
	 */
	public String end() {
		return this.url+ this.total_page;
	}

	/*
	 * 当前页的记录
	 */
	public String nowpage() {
		return "当前为第"+this.self_page+"页，第" + this.start_id + this.desc.get("unit") + "-" + this.end_id
				+ this.desc.get("unit");
	}
	/*
	 * 返回当前页码
	 */
	public int selfnum() {
		return this.self_page;
	}

	/*
	 * count 统计数据信息
	 */
	public String count() {
		return "<span>共"+ this.total_rows + "条记录</span>";
	}

	/*
	 * 获得页码数组
	 */
	public String pagelist() {
		StringBuffer str=new StringBuffer();
        int begin=this.self_page-(this.page_rows-1)/2;
        if(begin<=0){
            begin=1;
        }

        int end=this.self_page+(this.page_rows-1)/2;
        if(end>=this.total_page){
            end=this.total_page;
        }

        if(begin==1){
            end=this.total_page>this.page_rows?this.page_rows:this.total_page;
        }

        if(end==this.total_page){
            begin=(this.total_page-this.page_rows+1<=0)?1:this.total_page-this.page_rows+1;
        }
        for(int i=begin;i<=end;i++){
            if(i==this.self_page){
                str.append("<a href='"+this.url+i+"' class='checked' style='font-weight: bold;'>"+i+"</a> ");
            }else{
                str.append("<a href='"+this.url+i+"'>"+i+"</a> ");
            }
        }
        return str.toString();	
	}
	 /*
	  * 跳转页面
	  */
    public String pagesinput(){
		return this.url;
	}
}
