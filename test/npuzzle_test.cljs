(ns npuzzle.test
    (:require-macros [cemerick.cljs.test
                      :refer (is deftest with-test run-tests testing test-var)])
    (:require [cemerick.cljs.test :as t]
              [npuzzle :as np]))

(def b3 [1 6 0
         8 4 2
         7 5 3])

(deftest test-pos-to-idx
  (is (=(np/pos->idx 3 [1 2]) 7)))

(deftest test-get-blank-pos
  (is (=(np/get-blank-pos 3 b3) [2 0])))

(deftest test-eval-moves
  (is (=(np/eval-moves
         3 b3
         [[1 0] [0 0] [0 1] [0 2]]
         [2 0])
   [[8 1 6
     7 4 2
     0 5 3]
    [0 2]
    [6 1 8 7]])))

(deftest test-move-piece-to
  (is (=(np/move-piece
       3 b3 [1 0] 2)
     [2 4 6 2])))

(deftest test-move-piece-to1
  (is (=(np/move-piece
       3 [7 6 1
          2 4 8
          3 5 0] [0 0] 1)
     [8 4 6 1 4 6 2 7 1])))

(deftest test-move-piece-redge
  (is (=(np/move-piece-redge
       3 [1 2 8
          7 4 5
          9 0 3] [2 0] 3)
     [4 5 8 2
      5 8 3
      4])))

(deftest test-moves
  (is (=(np/moves 3 [2 2] [2 0])
        [[2 1] [2 0]])))

(deftest test-moves-masked
  (is (=(np/moves 3 [2 2] [2 0] #(not= % [2 1]))
        [[1 2] [1 1] [1 0] [2 0]])))
