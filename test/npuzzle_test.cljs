(ns npuzzle-test
    (:require-macros [cemerick.cljs.test
                      :refer (is deftest with-test run-tests testing test-var)])
    (:require [cemerick.cljs.test :as t]
              [cljs-unit-test.core :as core])
    )

(deftest test-sum-two-numbers
    (is (= (core/sum-two-numbers 2 2) 4))
    (is (= (core/sum-two-numbers 0 5) 5))
    )
