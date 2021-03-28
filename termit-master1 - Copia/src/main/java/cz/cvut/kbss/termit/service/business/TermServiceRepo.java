package cz.cvut.kbss.termit.service.business;

import java.util.Objects;

import cz.cvut.kbss.termit.model.Term;
import cz.cvut.kbss.termit.service.changetracking.ChangeRecordProvider;

public class TermServiceRepo {

	public TermServiceRepo() {
		super();
	}

	/**
	 * Updates the specified term.
	 *
	 * @param term Term update data
	 * @return The updated term
	 */
	public Term update(Term term) {
	    Objects.requireNonNull(term);
	    return repositoryService.update(term);
	}

}