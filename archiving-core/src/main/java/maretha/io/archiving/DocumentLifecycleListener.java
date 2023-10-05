package maretha.io.archiving;

import static java.util.Arrays.asList;
import static org.nuxeo.ecm.core.api.LifeCycleConstants.TRANSTION_EVENT_OPTION_TRANSITION;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ;

import java.util.List;

import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentLifecycleListener implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentLifecycleListener.class);

    List<String> acceptedEvents = asList("lifecycle_transition_event");

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        String eventName = event.getName();
        if (!acceptedEvents.contains(eventName)) {
            return;
        }

        DocumentModel doc = ((DocumentEventContext) ctx).getSourceDocument();
        if (doc == null) {
            return;
        }

        if ("lifecycle_transition_event".equals(eventName)) {
            // check if its sent to Inactive
            String transition = (String) ctx.getProperty(TRANSTION_EVENT_OPTION_TRANSITION);
            if (!"to_Archived".equals(transition)) {
                return;
            }
        }
        // move document

        CoreInstance.doPrivileged(Framework.getService(RepositoryManager.class).getDefaultRepositoryName(), session -> {
           //reset permissions
            DocumentModel document = session.getDocument(doc.getRef());
            //clear local permissions
            ACP acp = doc.getACP();
            ACL acl = acp.getOrCreateACL(ACL.LOCAL_ACL);
            acl.clear();
            document.setACP(acp, true);
            
            document = session.saveDocument(document);
            
            session.move(doc.getRef(), new PathRef("/Archive/workspaces/Archive"), null);
        });
    }

}
