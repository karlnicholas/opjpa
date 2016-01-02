package clread.memorydb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import opinions.model.OpinionKey;
import opinions.model.OpinionSummary;
import opinions.model.StatuteCitation;
import opinions.model.StatuteKey;
import opinions.parsers.ParserResults.PersistenceInterface;

public class MemoryDBFacade implements PersistenceInterface {
    
    protected MemoryDB dataBase;

    private MemoryDBFacade(){
        dataBase = new MemoryDB();
    }
    private static class SingletonHelper {
        private static final MemoryDBFacade INSTANCE = new MemoryDBFacade();
    }
    public static MemoryDBFacade getInstance(){
        return SingletonHelper.INSTANCE;
    }
    
	public void clearDB() {
		dataBase.getStatuteTable().clear();
		dataBase.getOpinionTable().clear();
	}
    public int getCount() {
        return dataBase.getStatuteTable().size();
    }

    public List<StatuteCitation> selectForCode(String code) {
        List<StatuteCitation> statutesForCode = Collections.synchronizedList(new ArrayList<StatuteCitation>());
        dataBase.getStatuteTable().stream().filter(new Predicate<StatuteCitation>() {
            @Override
            public boolean test(StatuteCitation codeCitation) {
            	if ( codeCitation.getStatuteKey().getCode() == null ) return false;
                return codeCitation.getStatuteKey().getCode().contains(code);
            }
        }).forEach(new Consumer<StatuteCitation>() {
            @Override
            public void accept(StatuteCitation codeCitation) {
                statutesForCode.add(codeCitation);
            }
        });
        return statutesForCode;
    }

    public StatuteCitation findStatuteByCodeSection(String code, String sectionNumber) {
        return statuteExists(new StatuteKey(code, sectionNumber));
    }

	@Override
	public StatuteCitation statuteExists(StatuteKey key) {
		return findStatuteByStatute(new StatuteCitation(key));
	}

	public StatuteCitation findStatuteByStatute(StatuteCitation statuteCitation) {
        StatuteCitation foundCitation = dataBase.getStatuteTable().floor(statuteCitation);
        if ( statuteCitation.equals(foundCitation)) return foundCitation;
        return null;
	}    

	@Override
	public void persistStatute(StatuteCitation statuteCitation) {
		dataBase.getStatuteTable().add(statuteCitation);
	}

	@Override
	public StatuteCitation mergeStatute(StatuteCitation statuteCitation) {
		// merge has already happened in the object itself
		dataBase.getStatuteTable().add(statuteCitation);
		return statuteCitation;
	}

	@Override
	public OpinionSummary opinionExists(OpinionKey key) {
        OpinionSummary tempOpinion = new OpinionSummary(key);
        if ( dataBase.getOpinionTable().contains(tempOpinion))
        	return dataBase.getOpinionTable().floor(tempOpinion);
        else return null;
	}

	@Override
	public void persistOpinion(OpinionSummary opinionSummary) {
		dataBase.getOpinionTable().add(opinionSummary);
	}

	@Override
	public OpinionSummary mergeOpinion(OpinionSummary opinionSummary) {
		dataBase.getOpinionTable().add(opinionSummary);
		return opinionSummary;
	}

	public Set<OpinionSummary> getAllOpinions() {
        return dataBase.getOpinionTable();
    }
	public Set<StatuteCitation> getAllStatutes() {
        return dataBase.getStatuteTable();
	}


}
