package processToText.dataModel.dsynt;


import br.edu.ufrgs.inf.bpm.builder.ProcessElementDocument;
import br.edu.ufrgs.inf.bpm.changes.templates.Lexemes;
import br.edu.ufrgs.inf.bpm.type.DSynTSentenceType;
import br.edu.ufrgs.inf.bpm.util.XmlFormat;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import processToText.dataModel.intermediate.ExecutableFragment;
import processToText.textPlanning.IntermediateToDSynTConverter;

import java.util.ArrayList;
import java.util.List;


public class DSynTMainSentence extends DSynTSentence {

    private Element root;
    private Document originalDSynT = null;

    public DSynTMainSentence(ExecutableFragment eFrag) {
        this.eFrag = eFrag;
        this.dSynTSentenceType = DSynTSentenceType.MAIN;
        createDSynTRepresentation();
    }

    public void createDSynTRepresentation() {

        // Create document
        doc = new DocumentImpl();
        root = doc.createElement("dsynts");
        doc.appendChild(root);

        // Create verb
        verb = IntermediateToDSynTConverter.createVerb(doc, eFrag, IntermediateToDSynTConverter.VERB_TYPE_MAIN);
        root.appendChild(verb);

        // Create business object
        if (eFrag.hasBO() == true) {
            object = IntermediateToDSynTConverter.createBO(doc, eFrag);
            verb.appendChild(object);
        }

        // Create role
        if (eFrag.getRole().equals("") == false) {
            role = IntermediateToDSynTConverter.createRole(doc, eFrag);
            verb.appendChild(role);
        }

        // Create addition
        if (eFrag.getAddition().equals("") == false) {
            IntermediateToDSynTConverter.createAddition(doc, verb, eFrag);
        }

        // Create mods
        if (eFrag.getAllMods() != null) {
            IntermediateToDSynTConverter.appendMods(doc, eFrag, verb, object);
        }

        // create additional sentences
        if (eFrag.getSentencList().size() > 0) {
            IntermediateToDSynTConverter.createAddSentences(doc, verb, eFrag);
        }

        originalDSynT = XmlFormat.getClone(doc);
    }

    public void changeRole() {
        // Create role
        if (!eFrag.getRole().equals("")) {
            verb.removeChild(role);
            role = IntermediateToDSynTConverter.createRole(doc, eFrag);
            verb.appendChild(role);

            // Change role in original dsynt
            updateRoleOriginalDSynt();
        }
    }

    private void updateRoleOriginalDSynt() {
        DSynTMainSentence dSynTUnique = new DSynTMainSentence(eFrag);
        dSynTUnique.getVerb().removeChild(dSynTUnique.getRole());
        Element newRole = IntermediateToDSynTConverter.createRole(dSynTUnique.doc, eFrag);
        dSynTUnique.getVerb().appendChild(newRole);
        originalDSynT = dSynTUnique.doc;

        if (this.getProcessElementDocumentList() != null && !this.getProcessElementDocumentList().isEmpty()) {
            ProcessElementDocument currentProcessDocument = this.getProcessElementDocumentList().get(0);
            currentProcessDocument.setDocument(originalDSynT);
        }
    }

    public void addCoordSentences(ArrayList<DSynTMainSentence> sentences) {
        if (sentences.size() == 1) {
            DSynTMainSentence coordSentence = sentences.get(0);
            addCoordSentences(coordSentence, Lexemes.SENTENCE_AGGREGATION, true);
        }
    }

    public void addCoordSentences(DSynTMainSentence coordSentence, String lexeme, boolean coordUseRole) {
        Element coordElement = doc.createElement("dsyntnode");
        coordElement.setAttribute("class", "coordinating_conj");
        coordElement.setAttribute("rel", "COORD");
        coordElement.setAttribute("lexeme", lexeme);
        verb.appendChild(coordElement);

        ExecutableFragment cFrag = coordSentence.getExecutableFragment();

        Element cVerb = IntermediateToDSynTConverter.createVerb(doc, cFrag, IntermediateToDSynTConverter.VERB_TYPE_SUBCONDITION);
        coordElement.appendChild(cVerb);

        Element cObject = IntermediateToDSynTConverter.createBO(doc, cFrag);
        cVerb.appendChild(cObject);

        if (!cFrag.getAddition().isEmpty()) {
            IntermediateToDSynTConverter.createAddition(doc, cVerb, cFrag);
        }

        if (coordUseRole) {
            Element cRole;
            if (eFrag.getRole().equals(cFrag.getRole())) {
                cRole = IntermediateToDSynTConverter.createSameRoleAggregation(doc);
                updateRoleCoordSentence(coordSentence);
            } else {
                cRole = IntermediateToDSynTConverter.createRole(doc, cFrag);
            }
            cVerb.appendChild(cRole);
        }

        updateProcessElementDocuments();
        addCoordProcessElementDocuments(coordSentence);
    }

    private void updateRoleCoordSentence(DSynTMainSentence coordSentence) {
        coordSentence.getVerb().removeChild(coordSentence.getRole());
        Element newRole = IntermediateToDSynTConverter.createSameRoleAggregation(coordSentence.doc);
        coordSentence.getVerb().appendChild(newRole);
    }

    private void updateProcessElementDocuments() {
        ProcessElementDocument currentProcessDocument = this.getProcessElementDocumentList().get(0);
        currentProcessDocument.setDocument(originalDSynT);
    }

    private void addCoordProcessElementDocuments(DSynTMainSentence coordSentence) {
        List<ProcessElementDocument> aggregatedElementDocumentList = coordSentence.getProcessElementDocumentList();
        this.getProcessElementDocumentList().addAll(aggregatedElementDocumentList);
    }

    public Element getVerb() {
        return verb;
    }

    public Document getDSynT() {
        return doc;
    }

    public ExecutableFragment getExecutableFragment() {
        return eFrag;
    }
}
