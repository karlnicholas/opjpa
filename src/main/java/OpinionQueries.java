

import java.util.*;

import javax.inject.Inject;
import javax.persistence.*;

import opca.model.OpinionKey;
import opca.model.OpinionSummary;
import opca.model.SlipOpinion;
import opca.model.StatuteCitation;
import opca.model.StatuteKey;
import opca.parsers.ParserResults.PersistenceInterface;

public class OpinionQueries implements PersistenceInterface {

	@Inject private EntityManager em;
/*	
	private TypedQuery<Date> listOpinionDates;
	private TypedQuery<StatuteCitation> findByCodeSection;
	private TypedQuery<StatuteCitation> selectForCode;
	private TypedQuery<StatuteCitation> findStatutesForKeys;
	private TypedQuery<OpinionSummary> findOpinionSummaryByOpinionKey;
	private TypedQuery<OpinionSummary> findOpinionsForKeys;
	private TypedQuery<SlipOpinion> findByOpinionDate;
	private TypedQuery<SlipOpinion> findByOpinionDateRange;
	private TypedQuery<SlipOpinion> findSlipOpinionByOpinionKey;
	
	@PostConstruct
	public void postConstruct() {
	    listOpinionDates = em.createNamedQuery("SlipOpinion.listOpinionDates", Date.class);
	    
	    findByCodeSection = em.createNamedQuery("StatuteCitation.findByCodeSection", StatuteCitation.class);
	    selectForCode = em.createNamedQuery("StatuteCitation.selectForCode", StatuteCitation.class);
	    findStatutesForKeys = em.createNamedQuery("StatuteCitation.findStatutesForKeys", StatuteCitation.class);
	    
	    findOpinionSummaryByOpinionKey = em.createNamedQuery("OpinionSummary.findByOpinionKey", OpinionSummary.class);
	    findOpinionsForKeys = em.createNamedQuery("OpinionSummary.findOpinionsForKeys", OpinionSummary.class);
	    
	    findByOpinionDate = em.createNamedQuery("SlipOpinion.findByOpinionDate", SlipOpinion.class);
	    findByOpinionDateRange = em.createNamedQuery("SlipOpinion.findByOpinionDateRange", SlipOpinion.class);
	    findSlipOpinionByOpinionKey = em.createNamedQuery("SlipOpinion.findByOpinionKey", SlipOpinion.class);
	}
*/
	
	// General
    public Long getCount() {
        return em.createQuery("select count(*) from StatuteCitation", Long.class).getSingleResult();
    }
	public List<Date> listPublishDates() {
		return em.createNamedQuery("SlipOpinion.listOpinionDates", Date.class).getResultList();
	}

	// StatuteCitation
	@Override
	public StatuteCitation statuteExists(StatuteKey key) {
		List<StatuteCitation> list = em.createNamedQuery("StatuteCitation.findByCodeSection", StatuteCitation.class)
				.setParameter("code", key.getCode())
				.setParameter("sectionNumber", key.getSectionNumber())
				.getResultList();
		if ( list.size() > 0 ) return list.get(0);
		return null;
	}

	@Override
	public List<StatuteCitation> getStatutes(Collection<StatuteKey> statuteKeys) {
		return em.createNamedQuery("StatuteCitation.findStatutesForKeys", StatuteCitation.class).setParameter("keys", statuteKeys).getResultList();
	}
	@Override
	public void persistStatute(StatuteCitation statute) {
		em.persist(statute);
	}
	@Override
	public StatuteCitation mergeStatute(StatuteCitation statute) {
		return em.merge(statute);
	}
	
	public StatuteCitation findStatute(StatuteKey key) {
		return em.createNamedQuery("StatuteCitation.findByCodeSection", StatuteCitation.class).setParameter("code", key.getCode()).setParameter("sectionNumber", key.getSectionNumber()).getSingleResult();
	}
    public List<StatuteCitation> selectForCode(String code) {
    	return em.createNamedQuery("StatuteCitation.selectForCode", StatuteCitation.class).setParameter("code", '%'+code+'%').getResultList();
    }

    public StatuteCitation testStatuteByCodeSection(String code, String sectionNumber) {
    	List<StatuteCitation> list = em.createNamedQuery("StatuteCitation.findByCodeSection", StatuteCitation.class).setParameter("code", code).setParameter("sectionNumber", sectionNumber).getResultList();
    	if ( list.size() > 0 ) return list.get(0);
    	else return null;
    }

	// OpinionSummary
	@Override
	public OpinionSummary opinionExists(OpinionKey key) {
		List<OpinionSummary> list = em.createNamedQuery("OpinionSummary.findByOpinionKey", OpinionSummary.class).setParameter("key", key).getResultList();
		if ( list.size() > 0 ) return list.get(0);
		return null;
	}
	@Override
	public List<OpinionSummary> getOpinions(Collection<OpinionKey> opinionKeys) {
		return em.createNamedQuery("OpinionSummary.findOpinionsForKeys", OpinionSummary.class).setParameter("keys", opinionKeys).getResultList();
	}
	@Override
	public void persistOpinion(OpinionSummary opinion) {
		em.persist(opinion);
	}
	@Override
	public OpinionSummary mergeOpinion(OpinionSummary opinion) {
		return em.merge(opinion);
	}
	public List<OpinionSummary> listOpinionSummaries() {
        return em.createQuery("select from OpinionSummary", OpinionSummary.class).getResultList();
    }
	public OpinionSummary findOpinion(OpinionKey key) {
		return em.createNamedQuery("OpinionSummary.findByOpinionKey", OpinionSummary.class).setParameter("key", key).getSingleResult();
	}
	
	// SlipOpinion
	public SlipOpinion findSlipOpinion(OpinionKey key) {
		return em.find(SlipOpinion.class, key);
	}
	public void removeSlipOpinions(List<SlipOpinion> oldOpinions) {
		for( SlipOpinion opinionSummary: oldOpinions ) {
			em.remove(opinionSummary);
		}
	}
	public void persistSlipOpinions(List<SlipOpinion> opinions) {
		for(SlipOpinion slipOpinion: opinions) {
			em.persist(slipOpinion);
		}
	}
	public void mergeAndPersistSlipOpinions(List<SlipOpinion> newOpinions) {
		Iterator<SlipOpinion> cit = newOpinions.iterator();
		while ( cit.hasNext() ) {
			SlipOpinion newOpinion = cit.next();
			em.persist(em.merge(newOpinion));
		}
	}
	public List<SlipOpinion> findByPublishDate(Date publishDate) {
		return em.createNamedQuery("SlipOpinion.findByOpinionDate", SlipOpinion.class).setParameter("publishDate", publishDate).getResultList();
	}

	public List<SlipOpinion> findByPublishDateRange(Date startDate, Date endDate) {
		return em.createNamedQuery("SlipOpinion.findByOpinionDateRange", SlipOpinion.class).setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();
	}
	
	public List<SlipOpinion> listSlipOpinions() {
		return em.createQuery("select from SlipOpinion", SlipOpinion.class).getResultList();
	}
	public SlipOpinion findSlipOpinionBySummaryKey(OpinionKey opinionSummaryKey) {
		return em.createNamedQuery("SlipOpinion.findByOpinionKey", SlipOpinion.class).setParameter("key", opinionSummaryKey).getSingleResult();
	}

	public SlipOpinion slipOpinionExists(OpinionKey opinionKey) {
		List<SlipOpinion> list = em.createNamedQuery("SlipOpinion.findByOpinionKey", SlipOpinion.class).setParameter("key", opinionKey).getResultList();
		if ( list.size() > 0 ) return list.get(0);
		return null;
	}

}
