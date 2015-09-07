(ns npuzzle
  (:require [tailrecursion.priority-map
             :refer [priority-map priority-map-by]]))

(def p (priority-map :a 2 :b 1 :c 3 :d 5 :e 4 :f 3))

(defn default-string [] (str (first p)))


(defn move [board from to]
  (assoc board
    from (board to)
    to (board from)))

(defn get-idx [board num] (.indexOf (to-array board) num))
(defn get-pos [n idx] [(mod idx n) (quot idx n)])

(defn can-move [n x0 y0 x1 y1]
  (= 1 (+ (Math/abs (- x0 x1)) (Math/abs (- y0 y1)))))

