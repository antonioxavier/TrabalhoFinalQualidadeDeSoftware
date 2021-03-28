package cz.cvut.kbss.termit.model.assignment;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.termit.model.Asset;
import cz.cvut.kbss.termit.model.selector.Selector;
import cz.cvut.kbss.termit.util.Vocabulary;

import java.util.Set;

@OWLClass(iri = Vocabulary.s_c_cil_vyskytu)
public abstract class OccurrenceTarget extends Selectors {

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = Vocabulary.s_p_ma_selektor, cascade = CascadeType.ALL, fetch = FetchType.EAGER) Set<Selector> selectors;

    public OccurrenceTarget() {
    }

    public OccurrenceTarget(Asset source) {
        super(source);
    }

    

    @Override
    public String toString() {
        return "OccurrenceTarget{" +
                "selectors=" + selectors +
                "} " + super.toString();
    }
}
