package listeners;

import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.action.SetPropertiesAction;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ExpireListener implements EventListener {

    @Override
    public void handleEvent(Event event) {
        if (event == null) return;

        var eventContext = event.getContext();

        DocumentModelList documentModels = expiryCheck(openSession());
        documentModels.forEach(documentModel -> {
            documentModel.followTransition("to_Archived");
        });

        String currentDate = ZonedDateTime.now(ZoneOffset.UTC).toString();

        // build command
        BulkCommand command = new BulkCommand.Builder(SetPropertiesAction.ACTION_NAME,
                "SELECT * FROM Document WHERE " +
                        "AND dc:expire <= DATE '" + currentDate + "' " +
                        "AND ecm:isProxy = 0 AND ecm:isTrashed = 0").repository("default")
                .user("Administrator")
                .param("dc:expire", ZonedDateTime.now(ZoneOffset.UTC).plusDays(3).toString())
                .build();

// run command
        BulkService bulkService = Framework.getService(BulkService.class);
        String commandId = bulkService.submit(command);
    }

    private DocumentModelList expiryCheck(CoreSession session) {
        String currentDate = ZonedDateTime.now(ZoneOffset.UTC).toString();
        return session.query("SELECT * FROM Document WHERE " +
                "AND dc:expire <= DATE '" + currentDate + "' " +
                "AND ecm:isProxy = 0 AND ecm:isTrashed = 0");
    }

    private CoreSession openSession() {
        Repository repository = Framework.getService(RepositoryManager.class).getDefaultRepository();
        if (repository != null) {
            return CoreInstance.getCoreSession(repository.getName());
        }
        throw new NuxeoException("Unable to open session");
    }
}
