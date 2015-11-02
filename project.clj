(defproject npuzzle "1.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2030"]
                 [org.clojure/core.async "0.1.256.0-1bf8cf-alpha"]]
  :plugins [[lein-cljsbuild "0.3.2"]
            [com.cemerick/clojurescript.test "0.3.3"]
            [lein-marginalia "0.8.0"]]
  :cljsbuild {
              :builds [{:source-paths ["src"]
                        :compiler {
                                   :output-to "out/main.js"
                                   :output-dir "out"
                                   :optimizations :none
                                   :source-map "out/main.js.map"
                                   :pretty-print true}}]})

