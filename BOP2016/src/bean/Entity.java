package bean;

import java.util.List;

public class Entity {
    private long Id;
    private long CId;
    private long CC;
    private long JId;
    private List<Long> FId;
    private List<Long> AuId;
    private List<Long> RId;
    
    public Entity(){}

	public long getId() {
		return Id;
	}

	public void setId(long id) {
		Id = id;
	}

	public long getCId() {
		return CId;
	}

	public void setCId(long cId) {
		CId = cId;
	}

	public long getJId() {
		return JId;
	}

	public void setJId(long jId) {
		JId = jId;
	}

	public List<Long> getFId() {
		return FId;
	}

	public void setFId(List<Long> fId) {
		FId = fId;
	}

	public List<Long> getAuId() {
		return AuId;
	}

	public void setAuId(List<Long> auId) {
		AuId = auId;
	}

	public List<Long> getRId() {
		return RId;
	}

	public void setRId(List<Long> rId) {
		RId = rId;
	}
	
	public long getCC() {
		return CC;
	}
	
	public void setCC(long cC) {
		CC = cC;
	}

	@Override
	public String toString() {
		return "Entity [Id=" + Id + ", CId=" + CId + ", CC=" + CC + ", JId=" + JId + ", FId=" + FId + ", AuId=" + AuId
				+ ", RId=" + RId + "]";
	}
}
