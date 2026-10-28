;; A Niva REPL in one JVM, started by tools/repl-jvm with the project's
;; root. Each line you enter is built by NIN, called in this JVM, with
;; --repl-line, so only that line runs, and loaded into the session here.
;; A line is built as resN = line first, and if it builds, its value is
;; printed as echo prints it; if not, it's built as it is, as a
;; declaration or an assignment. A line that fails to build or run isn't
;; kept. Ctrl-D ends the session.
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

;; Writes these lines to repl.niva and builds its REPL module with these
;; options. NIN prints why a build fails, and exits 0 either way, so what
;; it prints is caught, and thrown when it wrote no module. The session's
;; own output is unaffected: Clojure's *out* holds the stream it started
;; with.
(defn build [lines & options]
  (spit session-file (str/join (map #(str % "\n") lines)))
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
      (throw (ex-info (str printed) {})))))

;; Builds these lines for the last one alone.
(defn build-line [lines]
  (build lines (str "--repl-line=" (count lines))))

;; The compiler's message, or the error's, without NIN's stack trace or
;; the project's path.
(defn trimmed [message]
  (->> (str/split-lines (str/replace message (str root "/") ""))
       (remove #(or (str/starts-with? % "Method:") (re-matches #"-*| *" %)))
       (take 6)))

;; Runs f, and prints why if it fails, with the root cause of a run-time
;; error. Says whether it ran.
(defn ran? [f]
  (try (f)
       true
       (catch Throwable e
         (let [cause (loop [e e] (if-let [c (ex-cause e)] (recur c) e))]
           (run! println (trimmed (or (ex-message cause) (str cause)))))
         false)))

;; A variable's declaration, x = …, x::T = … or mut x = …, or an
;; assignment, x <- …. NIN builds a declaration after resN = too, but with
;; x local to resN's value, and checks an assignment there as far as the
;; Clojure backend, so these are built as they are.
(def declaration #"\s*(mut\s+)?\w+(::\S+)?\s*(=(?!=)|<-).*")

;; Prints resN, the line's value, as echo does, unless it's Unit, which
;; NIN's Clojure writes :unit, or echo, the line's last message, printed
;; it already.
(defn show [n]
  (let [name (str "res" n)
        value @(ns-resolve 'niva.repl.session (symbol name))]
    (when-not (or (= value :unit)
                  (str/includes? (slurp module) (str "(def " name " (niva.core/Any__echo")))
      ((resolve 'niva.core/Any__echo) value))))

;; Builds a line and runs it: as resN = line if that builds, and prints
;; its value, or else as it is. Returns the line in the form that ran, or
;; nil if it failed.
(defn enter [kept n line]
  (let [named (str "res" n " = " line)
        value? (and (not (re-matches declaration line))
                    (try (build-line (conj kept named))
                         true
                         (catch Throwable _ false)))]
    (when (ran? #(do (when-not value? (build-line (conj kept line)))
                     (load-file (str module))
                     (when value? (show n))))
      (if value? named line))))

;; The prompt shows only on a terminal, as bash's read -p does, so lines
;; piped in print only their output.
(def prompt (when (some-> (System/console) .isTerminal) "niva> "))

(when-not (ran? #(do (build [])
                      (load-file (str module))))
  (System/exit 1))
;; n counts the lines kept as resN = line, from res1.
(loop [kept [] n 1]
  (when prompt
    (print prompt)
    (flush))
  (if-let [line (read-line)]
    (if (str/blank? line)
      (recur kept n)
      (if-let [form (enter kept n line)]
        (recur (conj kept form) (if (= form line) n (inc n)))
        (do (println "(not kept)")
            (recur kept n))))
    (println)))
