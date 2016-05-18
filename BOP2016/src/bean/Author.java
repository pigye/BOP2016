package bean;

import java.util.LinkedList;
import java.util.List;

public class Author {
	private List<Entity> Papers;
    private long AuId;
    private List<Long> AfId;
    
    public Author(){
        
    }
    public Author(long AuId,List<Long> AfId,List<Entity> Papers){
        this.Papers = Papers;
    	this.AuId = AuId;
    	this.AfId = AfId;
    }
    
    public void setPapers(List<Entity> Papers){
        this.Papers = Papers;
    }
    public List<Entity> getPapers(){
        return Papers;
    }
    
    public List<Long> getPapersId(){
        List <Long> ids = new LinkedList<>();
        for (Entity paper: Papers){
            ids.add(paper.getId());
        }
        return ids;
    }
    
    public void setAuId(long AuId){
        this.AuId = AuId;
    }
    public long getAuId(){
        return AuId;
    }
    
    public void setAfId(List<Long> AfId){
        this.AfId = AfId;
    }
    public List<Long> getAfId(){
        return AfId;
    }
	@Override
	public String toString() {
		return "Author [Papers=" + Papers + ", AuId=" + AuId + ", AfId=" + AfId + "]";
	}
}
