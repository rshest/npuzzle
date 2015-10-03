(defproject npuzzle "0.1.1"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2030"]
                 [org.clojure/core.async "0.1.256.0-1bf8cf-alpha"]
                 [tailrecursion/cljs-priority-map "1.1.0"]]
  :plugins [[lein-cljsbuild "0.3.2"]
            [com.cemerick/clojurescript.test "0.3.3"]
            [lein-marginalia "0.8.0"]]
  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src"]
                        :compiler {
                                   :output-to "out/main.js"
                                   :output-dir "out"
                                   :optimizations :none
                                   :cache-analysis true
                                   :source-map true}}
                       {:id "release"
                        :source-paths ["src"]
                        :compiler {
                                   :output-to "out-release/main.min.js"
                                   :output-dir "out-release"
                                   :optimizations :advanced
                                   :pretty-print false}}
                       {:id "test"
                        :source-paths ["src" "test"]
                        :notify-command ["D:/NotBackedUp/tools/slimerjs/slimerjs.bat" :cljs.test/runner "out-test/main.test.js"]
                        :compiler {
                                   :output-to "out-test/main.test.js"
                                   :output-dir "out-test"
                                   :optimizations :whitespace
                                   :pretty-print true
                                   :source-map "out-test/main.test.js.map"
                                   :cache-analysis true}}]
              :test-commands {"test" ["D:/NotBackedUp/tools/slimerjs/slimerjs.bat" :runner "out-test/main.test.js"]}})

