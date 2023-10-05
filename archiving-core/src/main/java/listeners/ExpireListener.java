package listeners;

import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;

public class ExpireListener implements EventListener {

    private final WorkManager workManager = Framework.getService(WorkManager.class);

    @Override
    public void handleEvent(Event event) {
        if (event == null) return;

        var eventContext = event.getContext();

        //TODO call worker
        workManager.schedule(null, null);
    }
}
