package scoring;

import java.util.ArrayList;
import java.util.List;

import opca.model.StatuteKey;

public class StatuteCaseScoreList {
	private StatuteKey slipOpinionStatute;
	private int slipOpinionReferCount;
	private List<StatuteCaseScore> statuteCaseScoreList;
	public StatuteCaseScoreList() {
		statuteCaseScoreList = new ArrayList<StatuteCaseScore>();
	}
	
	public StatuteKey getSlipOpinionStatute() {
		return slipOpinionStatute;
	}
	public void setSlipOpinionStatute(StatuteKey slipOpinionStatute) {
		this.slipOpinionStatute = slipOpinionStatute;
	}
	public int getSlipOpinionReferCount() {
		return slipOpinionReferCount;
	}
	public void setSlipOpinionReferCount(int slipOpinionReferCount) {
		this.slipOpinionReferCount = slipOpinionReferCount;
	}
	public List<StatuteCaseScore> getStatuteCaseScoreList() {
		return statuteCaseScoreList;
	}
	public void setStatuteCaseScoreList(List<StatuteCaseScore> statuteCaseScoreList) {
		this.statuteCaseScoreList = statuteCaseScoreList;
	}

}
