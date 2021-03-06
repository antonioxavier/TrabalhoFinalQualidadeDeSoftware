/**
 * TermIt Copyright (C) 2019 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <https://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.termit.service.repository;

import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.termit.environment.Environment;
import cz.cvut.kbss.termit.environment.Generator;
import cz.cvut.kbss.termit.exception.ResourceExistsException;
import cz.cvut.kbss.termit.exception.ValidationException;
import cz.cvut.kbss.termit.exception.VocabularyImportException;
import cz.cvut.kbss.termit.model.Term;
import cz.cvut.kbss.termit.model.Vocabulary;
import cz.cvut.kbss.termit.model.changetracking.AbstractChangeRecord;
import cz.cvut.kbss.termit.model.changetracking.PersistChangeRecord;
import cz.cvut.kbss.termit.service.BaseServiceTestRunner;
import cz.cvut.kbss.termit.service.IdentifierResolver;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class VocabularyRepositoryServiceTest extends BaseServiceTestRunner {

    private VocabularyRepositoryServiceTestData data = new VocabularyRepositoryServiceTestData();

	@BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        this.data.user = Generator.generateUserAccountWithPassword();
        transactional(() -> data.em.persist(data.user));
        Environment.setCurrentUser(data.user);
    }

    @Test
    void persistGeneratesPersistChangeRecord() {
        final Vocabulary vocabulary = Generator.generateVocabularyWithId();
        data.sut.persist(vocabulary);

        final Vocabulary result = data.em.find(Vocabulary.class, vocabulary.getUri());
        assertNotNull(result);

        final PersistChangeRecord record = data.em
                .createQuery("SELECT r FROM PersistChangeRecord r WHERE r.changedEntity = :vocabularyIri",
                        PersistChangeRecord.class).setParameter("vocabularyIri", vocabulary.getUri()).getSingleResult();
        assertNotNull(record);
        assertEquals(data.user.toUser(), record.getAuthor());
        assertNotNull(record.getTimestamp());
    }

    @Test
    void persistThrowsValidationExceptionWhenVocabularyNameIsBlank() {
        final Vocabulary vocabulary = Generator.generateVocabularyWithId();
        vocabulary.setLabel("");
        final ValidationException exception = assertThrows(ValidationException.class, () -> data.sut.persist(vocabulary));
        assertThat(exception.getMessage(), containsString("label must not be blank"));
    }

    @Test
    void persistGeneratesIdentifierWhenInstanceDoesNotHaveIt() {
        final Vocabulary vocabulary = Generator.generateVocabulary();
        data.sut.persist(vocabulary);
        assertNotNull(vocabulary.getUri());

        final Vocabulary result = data.em.find(Vocabulary.class, vocabulary.getUri());
        assertNotNull(result);
        assertThat(result.getUri().toString(), containsString(IdentifierResolver.normalize(vocabulary.getLabel())));
    }

    @Test
    void persistDoesNotGenerateIdentifierWhenInstanceAlreadyHasOne() {
        final Vocabulary vocabulary = Generator.generateVocabularyWithId();
        final URI originalUri = vocabulary.getUri();
        data.sut.persist(vocabulary);
        assertNotNull(vocabulary.getUri());

        final Vocabulary result = data.em.find(Vocabulary.class, vocabulary.getUri());
        assertNotNull(result);
        assertEquals(originalUri, result.getUri());
    }

    @Test
    void persistCreatesGlossaryAndModelInstances() {
        final Vocabulary vocabulary = new Vocabulary();
        vocabulary.setUri(Generator.generateUri());
        vocabulary.setLabel("TestVocabulary");
        data.sut.persist(vocabulary);
        final Vocabulary result = data.em.find(Vocabulary.class, vocabulary.getUri());
        assertNotNull(result.getGlossary());
        assertNotNull(result.getModel());
    }

    @Test
    void persistThrowsResourceExistsExceptionWhenAnotherVocabularyWithIdenticalIdentifierAlreadyIriExists() {
        final Vocabulary vocabulary = Generator.generateVocabularyWithId();
        transactional(() -> data.em.persist(vocabulary));

        final Vocabulary toPersist = Generator.generateVocabulary();
        toPersist.setUri(vocabulary.getUri());
        assertThrows(ResourceExistsException.class, () -> data.sut.persist(toPersist));
    }

    @Test
    void updateThrowsValidationExceptionForEmptyName() {
        final Vocabulary vocabulary = Generator.generateVocabularyWithId();
        transactional(() -> data.em.persist(vocabulary, descriptorFor(vocabulary)));

        vocabulary.setLabel("");
        assertThrows(ValidationException.class, () -> data.sut.update(vocabulary));
    }

    private Descriptor descriptorFor(Vocabulary entity) {
        return data.descriptorFactory.vocabularyDescriptor(entity);
    }

    @Test
    void updateSavesUpdatedVocabulary() {
        final Vocabulary vocabulary = Generator.generateVocabularyWithId();
        transactional(() -> data.em.persist(vocabulary, descriptorFor(vocabulary)));

        final String newName = "Updated name";
        vocabulary.setLabel(newName);
        data.sut.update(vocabulary);
        final Vocabulary result = data.em.find(Vocabulary.class, vocabulary.getUri());
        assertNotNull(result);
        assertEquals(newName, result.getLabel());
    }

    @Test
    void removeRemovesNondocumentEmptyNonImportedVocabulary() {
        final Vocabulary vocabulary = Generator.generateVocabularyWithId();
        transactional(() -> data.em.persist(vocabulary, descriptorFor(vocabulary)));
        data.sut.remove(vocabulary);
        final Vocabulary result = data.em.find(Vocabulary.class, vocabulary.getUri());
        assertNull(result);
    }

    @Test
    void updateThrowsVocabularyImportExceptionWhenTryingToDeleteVocabularyImportRelationshipAndTermsAreStillRelated() {
        final Vocabulary subjectVocabulary = Generator.generateVocabularyWithId();
        final Vocabulary targetVocabulary = Generator.generateVocabularyWithId();
        subjectVocabulary.setImportedVocabularies(Collections.singleton(targetVocabulary.getUri()));
        final Term child = Generator.generateTermWithId();
        final Term parentTerm = Generator.generateTermWithId();
        child.addParentTerm(parentTerm);
        subjectVocabulary.getGlossary().addRootTerm(child);
        targetVocabulary.getGlossary().addRootTerm(parentTerm);
        transactional(() -> {
            data.em.persist(subjectVocabulary, data.descriptorFactory.vocabularyDescriptor(subjectVocabulary));
            data.em.persist(targetVocabulary, data.descriptorFactory.vocabularyDescriptor(targetVocabulary));
            child.setGlossary(subjectVocabulary.getGlossary().getUri());
            data.em.persist(child, data.descriptorFactory.termDescriptor(subjectVocabulary));
            parentTerm.setGlossary(targetVocabulary.getGlossary().getUri());
            data.em.persist(parentTerm, data.descriptorFactory.termDescriptor(targetVocabulary));
            Generator.addTermInVocabularyRelationship(child, subjectVocabulary.getUri(), data.em);
            Generator.addTermInVocabularyRelationship(parentTerm, targetVocabulary.getUri(), data.em);
        });

        subjectVocabulary.setImportedVocabularies(Collections.emptySet());
        assertThrows(VocabularyImportException.class, () -> data.sut.update(subjectVocabulary));
    }

    @Test
    void updateUpdatesVocabularyWithImportRemovalWhenNoRelationshipsBetweenTermsExist() {
        final Vocabulary subjectVocabulary = Generator.generateVocabularyWithId();
        final Vocabulary targetVocabulary = Generator.generateVocabularyWithId();
        subjectVocabulary.setImportedVocabularies(Collections.singleton(targetVocabulary.getUri()));
        final Term child = Generator.generateTermWithId();
        final Term parentTerm = Generator.generateTermWithId();
        subjectVocabulary.getGlossary().addRootTerm(child);
        child.setVocabulary(subjectVocabulary.getUri());
        targetVocabulary.getGlossary().addRootTerm(parentTerm);
        parentTerm.setVocabulary(targetVocabulary.getUri());
        transactional(() -> {
            data.em.persist(subjectVocabulary, data.descriptorFactory.vocabularyDescriptor(subjectVocabulary));
            data.em.persist(targetVocabulary, data.descriptorFactory.vocabularyDescriptor(targetVocabulary));
            child.setGlossary(subjectVocabulary.getGlossary().getUri());
            data.em.persist(child, data.descriptorFactory.termDescriptor(subjectVocabulary));
            parentTerm.setGlossary(targetVocabulary.getGlossary().getUri());
            data.em.persist(parentTerm, data.descriptorFactory.termDescriptor(targetVocabulary));
            Generator.addTermInVocabularyRelationship(child, subjectVocabulary.getUri(), data.em);
            Generator.addTermInVocabularyRelationship(parentTerm, targetVocabulary.getUri(), data.em);
        });

        subjectVocabulary.setImportedVocabularies(Collections.emptySet());
        data.sut.update(subjectVocabulary);
        assertThat(data.em.find(Vocabulary.class, subjectVocabulary.getUri()).getImportedVocabularies(),
                anyOf(nullValue(), IsEmptyCollection.empty()));
    }

    @Test
    void getTransitivelyImportedVocabulariesReturnsEmptyCollectionsWhenVocabularyHasNoImports() {
        final Vocabulary subjectVocabulary = Generator.generateVocabularyWithId();
        transactional(() -> data.em.persist(subjectVocabulary, data.descriptorFactory.vocabularyDescriptor(subjectVocabulary)));
        final Collection<URI> result = data.sut.getTransitivelyImportedVocabularies(subjectVocabulary);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLastModifiedReturnsInitializedValue() {
        final long result = data.sut.getLastModified();
        assertThat(result, greaterThan(0L));
        assertThat(result, lessThanOrEqualTo(System.currentTimeMillis()));
    }

    @Test
    void getChangesRetrievesChangesForVocabulary() {
        final Vocabulary vocabulary = Generator.generateVocabularyWithId();
        transactional(() -> data.em.persist(vocabulary, data.descriptorFactory.vocabularyDescriptor(vocabulary)));
        final List<AbstractChangeRecord> changes = data.sut.getChanges(vocabulary);
        assertTrue(changes.isEmpty());
    }
}
