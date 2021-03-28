package cz.cvut.kbss.termit.service.repository;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.termit.model.UserAccount;
import cz.cvut.kbss.termit.persistence.DescriptorFactory;

public class VocabularyRepositoryServiceTestData {
	@Autowired
	public DescriptorFactory descriptorFactory;
	@Autowired
	public EntityManager em;
	@Autowired
	public VocabularyRepositoryService sut;
	public UserAccount user;

	public VocabularyRepositoryServiceTestData() {
	}
}