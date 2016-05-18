package bean;

public class AuIdandAfId {
    private long auids;
    private long afids;
    
    public AuIdandAfId(){
        
    }
    public AuIdandAfId(long auids, long afids){
    	this.auids = auids;
    	this.afids = afids;
    }
    public long getAuId(){
        return auids;
    }
    public long getAfId(){
        return afids;
    }
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (afids ^ (afids >>> 32));
		result = prime * result + (int) (auids ^ (auids >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuIdandAfId other = (AuIdandAfId) obj;
		if (afids != other.afids)
			return false;
		if (auids != other.auids)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "AuIdandAfId [auids=" + auids + ", afids=" + afids + "]";
	}
    
}
