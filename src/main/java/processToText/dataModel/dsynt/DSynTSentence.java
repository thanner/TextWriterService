package processToText.dataModel.dsynt;


import br.edu.ufrgs.inf.bpm.builder.ProcessElementDocument;
import br.edu.ufrgs.inf.bpm.builder.elementType.ProcessElementType;
import br.edu.ufrgs.inf.bpm.type.DSynTSentenceType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import processToText.dataModel.intermediate.ExecutableFragment;

import java.util.ArrayList;
import java.util.List;

public abstract class DSynTSentence {

    public static final int TYPE_IF = 0;
    public static final int TYPE_AS_LONG_AS = 1;
    public static final int TYPE_ONCE = 2;
    public static final int TYPE_WHETHER = 3;
    public static final int TYPE_NONE = 4;
    public static final int TYPE_WHEN = 5;
    public boolean bo_replaceWithPronoun = false; //	private boolean boIsPronoun = false;
    public boolean bo_isSubject = false;//	private boolean boIsSubject = false;
    public boolean bo_isPlural = false;    //	private boolean boIsPlural = false;
    public boolean bo_hasArticle = true; //	private boolean boHasArticle = true;
    public boolean bo_hasIndefArticle = false; //	private boolean boHasIndefArticle = false;
    public boolean verb_isPassive = false;//	private boolean verbIsPassive = false;
    public boolean verb_isParticiple = false; //	private boolean verbIsParticiple = false;
    public boolean verb_isNegated = false;    //	private boolean negation = false;
    public boolean verb_isPast = false; //	private boolean isPast = false;
    public boolean sen_isCoord = true;//	private boolean isCoord = true;
    public boolean sen_hasConnective = false;
    public boolean sen_hasBullet = false;
    protected Document doc;
    protected ExecutableFragment eFrag;
    protected DSynTSentenceType dSynTSentenceType;
    protected Element verb;
    protected Element object;
    protected Element role;

    public Element getVerb() {
        return verb;
    }

    public Element getObject() {
        return object;
    }

    public Element getRole() {
        return role;
    }

    public Document getDSynT() {
        return doc;
    }

    public ExecutableFragment getExecutableFragment() {
        return eFrag;
    }

    /**
     * Thanner
     */

    public abstract void createDSynTRepresentation();

    private List<ProcessElementDocument> processElementDocumentList = new ArrayList<>();

    public List<ProcessElementDocument> getProcessElementDocumentList() {
        return processElementDocumentList;
    }

    public void setProcessElementDocumentList(List<ProcessElementDocument> processElementDocumentList) {
        this.processElementDocumentList = processElementDocumentList;
    }

    public void addProcessElementDocument(String processElementId, ProcessElementType processElementType) {
        addProcessElementDocument(processElementId, processElementType, eFrag.getRole().trim(), doc);
    }

    public void addProcessElementDocument(String processElementId, ProcessElementType processElementType, Document document) {
        addProcessElementDocument(processElementId, processElementType, eFrag.getRole().trim(), document);
    }

    public void addProcessElementDocument(String processElementId, ProcessElementType processElementType, String role, String sentence) {
        ProcessElementDocument processElementDocument = new ProcessElementDocument();
        processElementDocument.setProcessElementId(processElementId);
        processElementDocument.setProcessElementType(processElementType);
        processElementDocument.setResourceName(role);
        processElementDocument.setSentence(sentence);
        processElementDocumentList.add(processElementDocument);
    }

    public void addProcessElementDocument(String processElementId, ProcessElementType processElementType, String role, Document document) {
        ProcessElementDocument processElementDocument = new ProcessElementDocument();
        processElementDocument.setProcessElementId(processElementId);
        processElementDocument.setProcessElementType(processElementType);
        processElementDocument.setResourceName(role);
        processElementDocument.setDocument(document);
        processElementDocumentList.add(processElementDocument);
    }

    public void fixDocuments() {
        for (ProcessElementDocument processElementDocument : processElementDocumentList) {
            processElementDocument.fixDocument();
        }
    }

    public DSynTSentenceType getdSynTSentenceType() {
        return dSynTSentenceType;
    }

}
