(ns imagetool.imagetool
  (:refer-clojure :exclude [read-string])
  (:require [clojure.java.io :as io]
            [clojure.edn :refer [read-string]]
            [clojure.string :as str])
  (:import [java.nio.file Files Paths CopyOption StandardCopyOption FileAlreadyExistsException]
           [java.nio.file.attribute FileAttribute]))

(defn create-symbolic-link [output executable]
  (try
    (Files/createSymbolicLink (Paths/get (str output) (into-array String ["AppRun"]))
                              (Paths/get "" (into-array String ["usr" "bin" executable]))
                              (make-array FileAttribute 0))
    (catch FileAlreadyExistsException e)))

(defn copy-directory-tree [input output]
  (doseq [file (file-seq input)
          :let [destination (Paths/get (str output) (into-array String ["usr"]))
                relative-path (.relativize (.toPath input) (.toPath file))
                destination-path (.resolve destination relative-path)]]    
    (if (.isDirectory file)
      (Files/createDirectories destination-path (make-array FileAttribute 0))
      (try
        (Files/copy (.toPath file) destination-path (into-array CopyOption [StandardCopyOption/COPY_ATTRIBUTES]))
        (catch FileAlreadyExistsException e)))))

(defn write-desktop-file [edn project output]
  (let [desktop (get-in edn [:packaging :uberjar :appimage :desktop])
        lines (-> ["[Desktop Entry]" (str "Name=" project) (str "Exec=" project) (str "Icon=" project)]
                 (conj (str/join (for [[k v] desktop] (str (str/capitalize (name k)) "=" v "\n")))))]
    (spit (str output "/" project ".desktop") (str/join \newline lines))))

(defn copy-icon-file [edn project output]
  (let [path (get-in edn [:packaging :uberjar :appimage :icon])]
    (io/copy (io/file (str (System/getProperty "user.dir") "/" path)) (io/file (str output "/" project ".png")))))

(defn make-app-image [output]
  (let [pb (ProcessBuilder. ["/usr/bin/appimagetool" (str output)])]
    (.waitFor (-> pb .inheritIO .start))))

(defn jpackage->imagetool [input output]
  (println "input: " input "\noutput: " output)
  (let [edn (read-string (slurp "meyvn.edn"))
        project (get-in edn [:pom :artifact-id])
        input (io/file (str input "/" project))
        output (io/file (str output "/" project ".AppDir"))]
    (println "copying files....")
    (copy-directory-tree input output)
    (println "creating symbolic link")
    (create-symbolic-link output project)
    (println "writing desktop file")
    (write-desktop-file edn project output)
    (println "copying icon file")
    (copy-icon-file edn project output)
    (println "producing app image")
    (make-app-image output)))
