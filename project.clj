(defproject npuzzle "0.1.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2030"]
                 [org.clojure/core.async "0.1.256.0-1bf8cf-alpha"]
                 [tailrecursion/cljs-priority-map "1.1.0"]]
  :plugins [[lein-cljsbuild "0.3.2"]
            [com.cemerick/clojurescript.test "0.3.3"]
            [lein-marginalia "0.8.0"]]
  :cljsbuild
  {:builds
   [{:source-paths ["src/" "test/"]
     :compiler {:optimizations :none
                :pretty-print true
                :output-dir "out"
                :output-to "out/main.js"}}]})
