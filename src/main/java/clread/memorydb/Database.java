package clread.memorydb;

import java.util.concurrent.ConcurrentSkipListSet;

import javax.xml.bind.annotation.XmlRootElement;

import opinions.model.OpinionSummary;
import opinions.model.StatuteCitation;

import javax.xml.bind.annotation.XmlElement;

@XmlRootElement
public class Database {
    private ConcurrentSkipListSet<OpinionSummary> opinionTable = new ConcurrentSkipListSet<OpinionSummary>();
    private ConcurrentSkipListSet<StatuteCitation> statuteTable = new ConcurrentSkipListSet<StatuteCitation>();
    
    public ConcurrentSkipListSet<OpinionSummary> getOpinionTable() {
        return opinionTable;
    }
    @XmlElement
    public void setOpinionTable(ConcurrentSkipListSet<OpinionSummary> opinionTable) {
        this.opinionTable = opinionTable;
    }
    public ConcurrentSkipListSet<StatuteCitation> getStatuteTable() {
        return statuteTable;
    }
    @XmlElement
    public void setStatuteTable(ConcurrentSkipListSet<StatuteCitation> statuteTable) {
        this.statuteTable = statuteTable;
    }

}
