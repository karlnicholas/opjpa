package load;

import java.util.List;

import loadmodel.LoadOpinion;

public interface CourtListenerCallback {

	void callBack(List<LoadOpinion> clOps);
	void shutdown();

}