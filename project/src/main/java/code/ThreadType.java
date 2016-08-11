package code;

/**
 * Created by Andreas on 11.08.2016.
 *
 * Typen um die Weiterverarbeitenden Threads zu unterteilen.
 *
 * MANAGER    -> darf neue Threads starten (Koordinator)
 * WORKER     -> darf nur arbeiten, aber keine neuen Threads starten (Arbeiter)
 * UNSPECIFIC -> ein ganz normaler Thread
 */
public enum ThreadType {

    MANAGER,
    WORKER,
    UNSPECIFIC;

}
