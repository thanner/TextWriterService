package processToText.dataModel.dsynt;


import br.edu.ufrgs.inf.bpm.builder.ProcessElementDocument;
import br.edu.ufrgs.inf.bpm.changes.templates.Lexemes;
import br.edu.ufrgs.inf.bpm.type.DSynTSentenceType;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import processToText.dataModel.intermediate.ExecutableFragment;
import processToText.textPlanning.IntermediateToDSynTConverter;

import java.util.ArrayList;
import java.util.List;


public class DSynTMainSentence extends DSynTSentence {

    private Element root;

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

    }

    public void changeRole() {
        // Create role
        if (eFrag.getRole().equals("") == false) {
            verb.removeChild(role);
            role = IntermediateToDSynTConverter.createRole(doc, eFrag);
            verb.appendChild(role);
        }
    }

    public void addCoordSentences(ArrayList<DSynTMainSentence> sentences) {
        if (sentences.size() == 1) {
            DSynTMainSentence coordSentence = sentences.get(0);
            addCoordSentences(coordSentence, Lexemes.SENTENCE_AGGREGATION, true);
        }
    }

    public void addCoordSentences(DSynTMainSentence coordSentence, String lexeme, boolean coordUseRole) {
        Element coord = doc.createElement("dsyntnode");
        coord.setAttribute("class", "coordinating_conj");
        coord.setAttribute("rel", "COORD");
        coord.setAttribute("lexeme", lexeme);
        verb.appendChild(coord);

        ExecutableFragment cFrag = coordSentence.getExecutableFragment();

        Element cVerb = IntermediateToDSynTConverter.createVerb(doc, cFrag, IntermediateToDSynTConverter.VERB_TYPE_SUBCONDITION);
        coord.appendChild(cVerb);

        Element cObject = IntermediateToDSynTConverter.createBO(doc, cFrag);
        cVerb.appendChild(cObject);

        if (coordUseRole) {
            Element cRole;
            if (eFrag.getRole().equals(cFrag.getRole())) {
                cRole = IntermediateToDSynTConverter.createSameRoleAggregation(doc);
            } else {
                cRole = IntermediateToDSynTConverter.createRole(doc, cFrag);
            }
            cVerb.appendChild(cRole);
        }

        addProcessElementDocuments(coordSentence);
    }

    private void addProcessElementDocuments(DSynTMainSentence coordSentence) {
        List<ProcessElementDocument> aggregatedElementDocumentList = coordSentence.getProcessElementDocumentList();
        for (ProcessElementDocument aggregatedElement : aggregatedElementDocumentList) {
            aggregatedElement.setDocument(this.getProcessElementDocumentList().get(0).getDocument());
        }
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
