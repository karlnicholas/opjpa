package scoring;

import opca.model.OpinionKey;

public class StatuteCaseScore {
	private OpinionKey opinionKey;
	private int opinionReferCount;
	public OpinionKey getOpinionKey() {
		return opinionKey;
	}
	public void setOpinionKey(OpinionKey opinionKey) {
		this.opinionKey = opinionKey;
	}
	public int getOpinionReferCount() {
		return opinionReferCount;
	}
	public void setOpinionReferCount(int opinionReferCount) {
		this.opinionReferCount = opinionReferCount;
	}
}
