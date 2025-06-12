package com.jbi.client;

import com.jbi.api.*;

/**
 * Exhaustive façade: one method per QueueServer REST endpoint.
 *
 * <p>Each method blocks, calls {@link RunEngineHttpClient#call(ApiEndpoint, Object)},
 * and returns the raw {@link Envelope}.  Pass {@code null} or {@link NoBody#INSTANCE}
 * when no JSON request body is needed.  The envelope’s {@code payload()} is
 * usually a {@code Map&lt;String,Object&gt;} (typed DTOs exist only for STATUS
 * and QUEUE_GET).</p>
 *
 * <p>All errors propagate as {@link RunEngineHttpClient.BlueskyException} or
 * {@link java.io.IOException} / {@link java.net.http.HttpTimeoutException}.</p>
 */
public final class RunEngineService {

    private final RunEngineHttpClient http = RunEngineHttpClient.get();

    /* ---- Ping & status --------------------------------------------------- */

    public Envelope<?>          ping()                       throws Exception { return http.call(ApiEndpoint.PING,            NoBody.INSTANCE); }
    public StatusResponse status() throws Exception { return http.send(ApiEndpoint.STATUS, NoBody.INSTANCE, StatusResponse.class); }
    public Envelope<?>          configGet()                  throws Exception { return http.call(ApiEndpoint.CONFIG_GET,      NoBody.INSTANCE); }

    /* ---- Queue control --------------------------------------------------- */

    public Envelope<?>          queueStart()                 throws Exception { return http.call(ApiEndpoint.QUEUE_START,     NoBody.INSTANCE); }
    public Envelope<?>          queueStop()                  throws Exception { return http.call(ApiEndpoint.QUEUE_STOP,      NoBody.INSTANCE); }
    public Envelope<?>          queueStopCancel()            throws Exception { return http.call(ApiEndpoint.QUEUE_STOP_CANCEL,NoBody.INSTANCE); }
    public Envelope<?>          queueGet()                   throws Exception { return http.call(ApiEndpoint.QUEUE_GET,       NoBody.INSTANCE); }
    public Envelope<?>          queueClear()                 throws Exception { return http.call(ApiEndpoint.QUEUE_CLEAR,     NoBody.INSTANCE); }
    public Envelope<?>          queueAutostart(Object body)  throws Exception { return http.call(ApiEndpoint.QUEUE_AUTOSTART, body); } // {"enable":true}
    public Envelope<?>          queueModeSet(Object body)    throws Exception { return http.call(ApiEndpoint.QUEUE_MODE_SET,  body); } // {"mode":"loop"}
    public Envelope<?>          queueItemAdd(Object rawBody) throws Exception { return http.call(ApiEndpoint.QUEUE_ITEM_ADD, rawBody);
    }

    public Envelope<?> queueItemAdd(QueueItem item,
                                    String   user,
                                    String   group) throws Exception {

        var req = new QueueItemAdd(
                QueueItemAdd.Item.from(item),
                user,
                group);

        return queueItemAdd(req);
    }
    public Envelope<?> queueItemAdd(QueueItem item) throws Exception {
        return queueItemAdd(item, "GUI Client", "primary");
    }

    public Envelope<?>          queueItemAddBatch(Object b ) throws Exception { return http.call(ApiEndpoint.QUEUE_ITEM_ADD_BATCH, b); }
    public Envelope<?>          queueItemGet(Object params ) throws Exception { return http.call(ApiEndpoint.QUEUE_ITEM_GET,  params); }
    public Envelope<?>          queueItemUpdate(Object b   ) throws Exception { return http.call(ApiEndpoint.QUEUE_ITEM_UPDATE, b); }
    public Envelope<?>          queueItemRemove(Object b   ) throws Exception { return http.call(ApiEndpoint.QUEUE_ITEM_REMOVE, b); }
    public Envelope<?>          queueItemRemoveBatch(Object b) throws Exception {return http.call(ApiEndpoint.QUEUE_ITEM_REMOVE_BATCH,b); }
    public Envelope<?>          queueItemMove(Object b     ) throws Exception { return http.call(ApiEndpoint.QUEUE_ITEM_MOVE,   b); }
    public Envelope<?>          queueItemMoveBatch(Object b) throws Exception { return http.call(ApiEndpoint.QUEUE_ITEM_MOVE_BATCH,b);}
    public Envelope<?>          queueItemExecute(Object b  ) throws Exception { return http.call(ApiEndpoint.QUEUE_ITEM_EXECUTE,b); }

    /* ---- History --------------------------------------------------------- */

    public Envelope<?>          historyGet()                 throws Exception { return http.call(ApiEndpoint.HISTORY_GET,      NoBody.INSTANCE); }
    public Envelope<?>          historyClear()               throws Exception { return http.call(ApiEndpoint.HISTORY_CLEAR,    NoBody.INSTANCE); }

    /* ---- Environment ----------------------------------------------------- */

    public Envelope<?>          environmentOpen()            throws Exception { return http.call(ApiEndpoint.ENVIRONMENT_OPEN,   NoBody.INSTANCE); }
    public Envelope<?>          environmentClose()           throws Exception { return http.call(ApiEndpoint.ENVIRONMENT_CLOSE,  NoBody.INSTANCE); }
    public Envelope<?>          environmentDestroy()         throws Exception { return http.call(ApiEndpoint.ENVIRONMENT_DESTROY,NoBody.INSTANCE); }
    public Envelope<?>          environmentUpdate(Object b)  throws Exception { return http.call(ApiEndpoint.ENVIRONMENT_UPDATE, b); }

    /* ---- Run Engine control --------------------------------------------- */

    public Envelope<?>          rePause()                    throws Exception { return http.call(ApiEndpoint.RE_PAUSE,  NoBody.INSTANCE); }
    public Envelope<?>          reResume()                   throws Exception { return http.call(ApiEndpoint.RE_RESUME, NoBody.INSTANCE); }
    public Envelope<?>          reStop()                     throws Exception { return http.call(ApiEndpoint.RE_STOP,   NoBody.INSTANCE); }
    public Envelope<?>          reAbort()                    throws Exception { return http.call(ApiEndpoint.RE_ABORT,  NoBody.INSTANCE); }
    public Envelope<?>          reHalt()                     throws Exception { return http.call(ApiEndpoint.RE_HALT,   NoBody.INSTANCE); }
    public Envelope<?>          reRuns(Object body)          throws Exception { return http.call(ApiEndpoint.RE_RUNS,   body); }

    /* ---- Permissions & allowed lists ------------------------------------ */

    public Envelope<?>          plansAllowed()               throws Exception { return http.call(ApiEndpoint.PLANS_ALLOWED,   NoBody.INSTANCE); }
    public Envelope<?>          devicesAllowed()             throws Exception { return http.call(ApiEndpoint.DEVICES_ALLOWED, NoBody.INSTANCE); }
    public Envelope<?>          plansExisting()              throws Exception { return http.call(ApiEndpoint.PLANS_EXISTING,  NoBody.INSTANCE); }
    public Envelope<?>          devicesExisting()            throws Exception { return http.call(ApiEndpoint.DEVICES_EXISTING,NoBody.INSTANCE); }
    public Envelope<?>          permissionsReload()          throws Exception { return http.call(ApiEndpoint.PERMISSIONS_RELOAD,NoBody.INSTANCE); }
    public Envelope<?>          permissionsGet()             throws Exception { return http.call(ApiEndpoint.PERMISSIONS_GET,   NoBody.INSTANCE); }
    public Envelope<?>          permissionsSet(Object body ) throws Exception { return http.call(ApiEndpoint.PERMISSIONS_SET,   body); }

    /* ---- Script / function ---------------------------------------------- */

    public Envelope<?>          scriptUpload(Object body  )  throws Exception { return http.call(ApiEndpoint.SCRIPT_UPLOAD,     body); }
    public Envelope<?>          functionExecute(Object b )   throws Exception { return http.call(ApiEndpoint.FUNCTION_EXECUTE,  b); }

    /* ---- Tasks ----------------------------------------------------------- */

    public Envelope<?>          taskStatus(Object params)    throws Exception { return http.call(ApiEndpoint.TASK_STATUS, params); }
    public Envelope<?>          taskResult(Object params)    throws Exception { return http.call(ApiEndpoint.TASK_RESULT, params); }

    /* ---- Locks ----------------------------------------------------------- */

    public Envelope<?>          lock(Object body   )         throws Exception { return http.call(ApiEndpoint.LOCK,   body); }
    public Envelope<?>          unlock()                     throws Exception { return http.call(ApiEndpoint.UNLOCK, NoBody.INSTANCE); }
    public Envelope<?>          lockInfo()                   throws Exception { return http.call(ApiEndpoint.LOCK_INFO, NoBody.INSTANCE); }

    /* ---- Kernel / Manager ------------------------------------------------ */

    public Envelope<?>          kernelInterrupt()            throws Exception { return http.call(ApiEndpoint.KERNEL_INTERRUPT, NoBody.INSTANCE); }
    public Envelope<?>          managerStop()                throws Exception { return http.call(ApiEndpoint.MANAGER_STOP,     NoBody.INSTANCE); }
    public Envelope<?>          managerKill()                throws Exception { return http.call(ApiEndpoint.MANAGER_KILL,     NoBody.INSTANCE); }

    /* ---- Auth ------------------------------------------------------------ */

    public Envelope<?>          sessionRefresh(Object b )    throws Exception { return http.call(ApiEndpoint.SESSION_REFRESH, b); }
    public Envelope<?>          apikeyNew     (Object body ) throws Exception { return http.call(ApiEndpoint.APIKEY_NEW,     body); }
    public Envelope<?>          apikeyInfo()                  throws Exception { return http.call(ApiEndpoint.APIKEY_INFO,    NoBody.INSTANCE); }
    public Envelope<?>          apikeyDelete  (Object params) throws Exception { return http.call(ApiEndpoint.APIKEY_DELETE,  params); }
    public Envelope<?>          whoAmI()                      throws Exception { return http.call(ApiEndpoint.WHOAMI,        NoBody.INSTANCE); }
    public Envelope<?>          apiScopes()                   throws Exception { return http.call(ApiEndpoint.API_SCOPES,    NoBody.INSTANCE); }
    public Envelope<?>          logout()                      throws Exception { return http.call(ApiEndpoint.LOGOUT,       NoBody.INSTANCE); }

    /* ---- Helpers --------------------------------------------------------- */

    public QueueGetPayload queueGetTyped() throws Exception {
        return http.send(ApiEndpoint.QUEUE_GET, NoBody.INSTANCE, QueueGetPayload.class);
    }

}
