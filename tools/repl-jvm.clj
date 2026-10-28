;; A Niva REPL in one JVM, started by tools/repl-jvm with the project's
;; root. Each line you enter is built by NIN, called in this JVM, with
;; --repl-line, so only that line runs, and loaded into the session here.
;; A line that fails to build or run isn't kept. Ctrl-D ends the session.
;;
;; The session is written to repl.niva at the project root, where the
;; compiler finds mpl/, and removed at the end, with the temporary folder
;; this JVM runs in. While it runs, other builds see its declarations too.
(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(def root (first *command-line-args*))
(def work (io/file (System/getProperty "user.dir")))
(def session-file (io/file root "repl.niva"))
(def module (io/file ".niva_clj/main.clj"))

(defn remove-work []
  (doseq [f (reverse (file-seq work))]
    (io/delete-file f true)))

(when (.exists session-file)
  (binding [*out* *err*]
    (println "repl-jvm: repl.niva exists, from another session or one that didn't end; remove it first"))
  (remove-work)
  (System/exit 1))
(.addShutdownHook (Runtime/getRuntime)
                  (Thread. #(do (io/delete-file session-file true)
                                (remove-work))))

;; Builds repl.niva's REPL module with these options and loads it. NIN
;; prints why a build fails, and exits 0 either way, so what it prints is
;; caught, and thrown when it wrote no module. The session's own output
;; is unaffected: Clojure's *out* holds the stream it started with.
(defn run [& options]
  (let [printed (java.io.ByteArrayOutputStream.)
        out System/out
        err System/err]
    (io/delete-file module true)
    (System/setOut (java.io.PrintStream. printed true))
    (System/setErr (java.io.PrintStream. printed true))
    (try (mainNiva.MainKt/main
          (into-array String (list* "build" (str session-file) "--backend=clj" "--repl" options)))
         (finally (System/setOut out) (System/setErr err)))
    (when-not (.exists module)
      (throw (ex-info (str printed) {})))
    (load-file (str module))))

;; The compiler's message, or the error's, without NIN's stack trace or
;; the project's path.
(defn trimmed [message]
  (->> (str/split-lines (str/replace message (str root "/") ""))
       (remove #(or (str/starts-with? % "Method:") (re-matches #"-*| *" %)))
       (take 6)))

;; Runs these options, and prints why if it fails, with the root cause of
;; a run-time error. Says whether it ran.
(defn ran? [& options]
  (try (apply run options)
       true
       (catch Throwable e
         (let [cause (loop [e e] (if-let [c (ex-cause e)] (recur c) e))]
           (run! println (trimmed (or (ex-message cause) (str cause)))))
         false)))

;; The prompt shows only on a terminal, as bash's read -p does, so lines
;; piped in print only their output.
(def prompt (when (some-> (System/console) .isTerminal) "niva> "))

(spit session-file "")
(when-not (ran?)
  (System/exit 1))
(loop [kept []]
  (when prompt
    (print prompt)
    (flush))
  (if-let [line (read-line)]
    (if (str/blank? line)
      (recur kept)
      (do (spit session-file (str/join (map #(str % "\n") (conj kept line))))
          (if (ran? (str "--repl-line=" (inc (count kept))))
            (recur (conj kept line))
            (do (println "(not kept)")
                (recur kept)))))
    (println)))
