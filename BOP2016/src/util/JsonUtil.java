package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import API.Evaluate;
import bean.AuIdandAfId;
import bean.Author;
import bean.Entity;

public class JsonUtil {
	public static final long NONE = -10086;
	public ObjectMapper mapper = new ObjectMapper();
	
	public JsonNode toJson(String str) {
		JsonNode node = null;
		try {
			node = mapper.readTree(str).get("entities");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (node == null) System.out.println(str);
		return node;
	}
	
	public long getId(JsonNode object) {
		return object.has("Id")?object.get("Id").asLong():NONE;
	}
	
	public long getCC(JsonNode object) {
		return object.has("CC")?object.get("CC").asLong():NONE;
	}
	
	public long getCId(JsonNode object) {
		JsonNode c = object.get("C");
		return c == null?NONE:c.get("CId").asLong();
	}
	
	public long getJId(JsonNode object) {
		JsonNode j = object.get("J");
		return j == null?NONE:j.get("JId").asLong();
	}
	
	public List<Long> getFIds(JsonNode object) {
		List<Long> fids = new LinkedList<Long>();
		if(object.has("F")){
			JsonNode array = object.get("F");
			for (int i = 0;i < array.size();i ++) {
				JsonNode item = array.get(i);
				fids.add(item.get("FId").asLong());
			}
		}
		return fids;
	}
	
	public List<Long> getAuIds(JsonNode object) {
		List<Long> auids = new LinkedList<Long>();
		if(object.has("AA")){
			JsonNode array = object.get("AA");
			for (int i = 0;i < array.size();i ++) {
				JsonNode item = array.get(i);
				auids.add(item.get("AuId").asLong());
			}
		}
		return auids;
	}
	
	public HashSet <Long> getAfIds(JsonNode object, long AuId){
	    HashSet <Long> afids = new HashSet<Long>();
	    if (object.has("AA")){
	        JsonNode array = object.get("AA");
	        for (int i = 0;i < array.size(); i++){
	            JsonNode item = array.get(i);
	            if (item.has("AfId") && item.get("AuId").asLong() == AuId)
	            afids.add(item.get("AfId").asLong());
	        }
	    }
	    return afids;
	}
	
	public List <AuIdandAfId> getAfIdsandAuIds(JsonNode object){
	    List<AuIdandAfId> list = new LinkedList<AuIdandAfId>();
	    if (object.has("AA")){
	    	JsonNode array = object.get("AA");
	        for (int i = 0; i < array.size(); i++){
	        	JsonNode item = array.get(i);
	            if (item.has("AfId")){
	                AuIdandAfId res = new AuIdandAfId(item.get("AuId").asLong(), item.get("AfId").asLong());
	                list.add(res);
	            }
	        }
	    }
	    return list;
	}
	
	public Set<AuIdandAfId> getAfIdsandAuIds(JsonNode object, List<Long> auids, List<Long> afids){
	    Set <AuIdandAfId> list = new HashSet<AuIdandAfId>();
	    if (object.has("AA")){
	    	JsonNode array = object.get("AA");
	        for (int i = 0; i < array.size(); i++){
	        	JsonNode item = array.get(i);
                if (item.has("AfId")){
                	long auid = item.get("AuId").asLong();
                	long afid = item.get("AfId").asLong();
                    if (auids.contains(auid) && afids.contains(afid)){
	                    AuIdandAfId res = new AuIdandAfId(auid, afid);
	                    list.add(res);
                    }
                }
	        }
	    }
	    return list;
	}

	
	public List<Long> getRIds(JsonNode object) {
		List<Long> rids = new LinkedList<Long>();
		if(object.has("RId")){
			JsonNode array = object.get("RId");
			for (int i = 0;i < array.size();i ++) {
				rids.add(array.get(i).asLong());
			}
		}
		return rids;
	}
	
	public void getEntity(JsonNode object, Entity entity) {
		entity.setId(getId(object));
		entity.setCC(getCC(object));
		entity.setCId(getCId(object));
		entity.setJId(getJId(object));
		entity.setFId(getFIds(object));
		entity.setRId(getRIds(object));
		entity.setAuId(getAuIds(object));
	}
	
//////获得一个作者一篇论文的AfId
	public Long getAfIds(JsonNode object,Long AuId) {
		Long afid = new Long(NONE);
		if(object.has("AA")){
			JsonNode array = object.get("AA");
			for (int i = 0;i < array.size();i ++) {
				JsonNode item = array.get(i);
				if(item.get("AuId").asLong() == AuId){
					return item.has("AfId")?item.get("AfId").asLong():NONE;
				}
			}
		}
		return afid;
	}
///////获得一个作者Author的所有信息,使用AuId进行evaluate API调用时
	public Author getAuthor(JsonNode array, Long AuId, Author author) {
		List<Entity> papers = new  LinkedList<Entity>();
		Set<Long> afIds = new  HashSet<Long>();
		for (int i = 0;i < array.size();i ++) {
			JsonNode object = array.get(i);
			Entity entity = new Entity();
			getEntity(object, entity);
			papers.add(entity);
			afIds.add(getAfIds(object, AuId));
		}
		afIds.remove(NONE);
		author.setPapers(papers);
		author.setAfId(new LinkedList<Long>(afIds));
		author.setAuId(AuId);
		return author;
	}
	
	public static void main(String[] args) {
		Evaluate evaluate = new Evaluate();
		JsonUtil util = new JsonUtil();
		String result = evaluate.evaluate("Id=2147152072", 0, "Id,AA.AuId,CC,F.FId,J.JId,C.CId,RId");
		JsonNode array = util.toJson(result);
		for (int i = 0;i < array.size();i ++) {
			JsonNode object = array.get(i);
			Entity entity = new Entity();
			util.getEntity(object, entity);
			System.out.println(entity);
		}
	}
	
	public void getAuthor(long AuId, JsonNode array, Author author){
	    HashSet <Long> afids = new HashSet<Long>();
	    List <Entity> papers = new LinkedList<>();
	            
	    for (int i = 0; i < array.size(); i++){
	    	Entity entity = new Entity();
			getEntity(array.get(i), entity);
	        papers.add(entity);
	        afids.addAll(getAfIds(array.get(i), AuId));
	    }
	    List <Long> afIds = new ArrayList<Long>(afids);
	    author.setAfId(afIds);
	    author.setPapers(papers);
	    author.setAuId(AuId);
	}
}
