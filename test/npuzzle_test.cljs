(ns npuzzle.test
    (:use [npuzzle])
    (:require-macros [cemerick.cljs.test
                      :refer (is deftest with-test run-tests testing test-var)])
    (:require [cemerick.cljs.test :as t]
              [npuzzle :as np]))

(deftest test-neighbors?
  (is (neighbors? [0 0] [1 0]))
  (is (neighbors? [1 1] [1 2]))
  (is (not (neighbors? [3 3] [3 3])))
  (is (not (neighbors? [3 3] [4 4]))))

(def b0 (make-board
          [1 6 0
           8 4 2
           7 5 3]))
(solve b0)

(deftest test-valid-pos?
  (is (valid-pos? b0 [0 0]))
  (is (valid-pos? b0 [2 0]))
  (is (not (valid-pos? b0 [3 0])))
  (is (not (valid-pos? b0 [-1 -1])))
  (is (not (valid-pos? b0 [4 2]))))

(deftest test-coor-convert
  (is (= 7 (pos->idx b0 [1 2])))
  (is (= [1 2] (idx->pos b0 7))))

(deftest test-board-get-at
  (is (= 7 (get-num-idx b0 5)))
  (is (= [1 2] (get-num-pos b0 5)))
  (is (= 5 (get-num-at b0 [1 2]))))

(deftest test-blank-op
  (is (= [2 0] (find-blank-pos b0)))
  (is (blank? b0 [2 0]))
  (is (not (blank? b0 [2 1]))))

(deftest test-move
  (is (= {:cells [1 6 2 8 4 0 7 5 3] :n 3 :m 3 :blank-pos [2 1]}
        (move b0 [2 1]))))

(deftest test-neighbors
  (is (= [[1 0] [0 1]] (neighbors b0 [0 0])))
  (is (= #{[0 1] [1 0] [2 1] [1 2]} (set (neighbors b0 [1 1])))))

(def b1 (make-board
          [1 6 3
           8 4 2
           7 5 0]))

(deftest test-num-solved?
  (is (= [true false true false false false true false true]
        (map (partial num-solved? b1) (range 0 9)))))

(deftest test-board-solved?
  (is (board-solved? (make-board [1 2 3 4 5 6 7 8 0])))
  (is (not (board-solved? (make-board (range 0 16)))))
  (is (not (board-solved? b0)))
  (is (not (board-solved? b1))))

(deftest test-board-valid?
  (is (cells-valid? (b0 :cells)))
  (is (cells-valid? (b1 :cells)))
  (is (cells-valid? (range 0 16)))
  (is (not (cells-valid? (range 1 17))))
  (is (not (cells-valid? [1 6 6 8 4 2 7 5 0]))))

(deftest test-path
  (is (= [[1 0] [1 1] [2 1]]
        (path b1 [0 0] [2 1])))
  (is (= nil (path b1 [1 1] [1 1])))
  (is (= [[1 1] [1 2]] (path b1 [1 0] [1 2]))))

(deftest test-path-masked
  (is (= [[1 0] [2 0] [2 1]]
        (path b1 [0 0] [2 1] #(not= [1 1] %))))
  (is (= [[0 1] [0 2] [1 2] [2 2] [2 1] [2 0]]
        (path b1 [0 0] [2 0] #(not (#{[1 0] [1 1]} %))))))

(deftest test-eval-moves
  (let [m (eval-moves b0 [[1 0] [0 0] [0 1] [0 2]])]
    (is (= ((first (take-last 1 m)) :cells)
             [8 1 6
              7 4 2
              0 5 3]))
    (is (= (count m) 4))))

(def b4 (make-board
          [1 6 0
           8 4 2
           7 5 3]))
(def b2 (make-board
          [7 6 1
           2 4 8
           3 5 0]))

(deftest test-move-num-middle
  (is (= (move-num-middle b4 [1 0] [2 0] (constantly true))
        [[1 0]]))
  (is (= (move-num-middle b4 [0 0] [1 0] (constantly true))
        [[1 0] [0 0]]))
  (is (= (move-num-middle b4 [2 1] [1 0] (constantly true))
        [[2 1] [1 1] [1 0] [2 0]]))
  (is (= (move-num-middle b2 [2 0] [0 0] (constantly true))
        [[2 1] [1 1] [1 0] [2 0] [2 1] [1 1] [0 1] [0 0] [1 0]])))

(def b5 (make-board
          [1 2 8
           7 4 5
           9 0 3]))
(def b5-1 (make-board
            [1 2 8
             7 0 3
             9 4 5]))

(deftest test-move-num-redge
  (is (= (move-num-redge b5 [2 2] [2 0] (constantly true))
        [[1 1] [2 1] [2 0] [1 0]
         [1 1] [2 1] [2 2]
         [1 2] [1 1] [1 0] [2 0] [2 1]])))


(def b6 (make-board
          [1 3 9
           4 2 0
           8 5 7]))
(deftest test-move-num-bedge
  (is (= (move-num-bedge b6 [2 2] [0 2] (constantly true))
        [[1 1] [1 2] [0 2] [0 1]
         [1 1] [1 2] [2 2]
         [2 1] [1 1] [0 1] [0 2] [1 2]])))

(deftest test-solve-order
  (is (= (solve-order 4 4)
        [[0 0] [1 0] [2 0] [3 0]
         [0 1] [0 2] [0 3]
         [1 1] [2 1] [3 1]
         [1 2] [1 3]
         [2 2] [3 2] [2 3]]))
  (is (= (solve-order 4 3)
        [[0 0] [0 1] [0 2]
         [1 0] [2 0] [3 0]
         [1 1] [1 2]
         [2 1] [3 1] [2 2]]))
  (is (= (solve-order 3 4)
        [[0 0] [1 0] [2 0]
         [0 1] [1 1] [2 1]
         [0 2] [0 3]
         [1 2] [2 2] [1 3]])))

(deftest test-top-left-edge?
  (is (top-left-edge? [3 1] [0 0]))
  (is (top-left-edge? [3 1] [0 1]))
  (is (top-left-edge? [3 1] [1 0]))
  (is (top-left-edge? [3 1] [2 1]))
  (is (top-left-edge? [1 3] [10 1]))
  (is (top-left-edge? [3 1] [2 1]))
  (is (not (top-left-edge? [3 1] [1 10])))
  (is (not (top-left-edge? [3 1] [4 1])))
  (is (not (top-left-edge? [3 1] [1 2])))
  (is (not (top-left-edge? [3 1] [3 1])))
  (is (not (top-left-edge? [3 1] [4 4]))))

(def b7 (make-board
          [6  14 3  13
           7  1  0  5
           8  10 2  12
           15 9  11 4 ]))
(def b8 (make-board
          [1   10  25  33  12  19
           14  2   34  26  17  6
           18  9   31  4   30  16
           28  3   21  22  35  24
           13  32  7    0  5   23
           27  8   20  11  29  15]))

(defn solve-res [b]
  (let [moves  (solve b)
        boards (eval-moves b moves)]
    (last boards)))

(deftest test-solve
  (is (board-solved? (solve-res b0)))
  (is (board-solved? (solve-res b7)))
  (is (board-solved? (solve-res b8))))

(run-tests)
