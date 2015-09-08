(ns npuzzle
  (:require [tailrecursion.priority-map
             :refer [priority-map priority-map-by]]))

(def DIRECTIONS [[-1 0] [0 -1] [1 0] [0 1]])

(def p (priority-map :a 2 :b 1 :c 3 :d 5 :e 4 :f 3))

(defn default-string [] (str (first p)))

(defn move [board from to]
  (assoc board
    from (board to)
    to (board from)))

(defn get-idx [board num] (.indexOf (to-array board) num))
(defn get-pos [n idx] [(mod idx n) (quot idx n)])


(defn manhattan-dist [x0 y0 x1 y1]
  (+ (Math/abs (- x0 x1)) (Math/abs (- y0 y1))))

(defn neighbor? [x0 y0 x1 y1]
  (= 1 (manhattan-dist x0 y0 x1 y1)))

(defn valid-cell? [n x y]
  (and (>= x 0) (< x n) (>= y 0) (< y n)))

(defn neighbors [n x y]
  (filter (fn [[cx cy]] (valid-cell? n cx cy))
          (map (fn [[dx dy]] [(+ x dx) (+ y dy)])
               DIRECTIONS)))

(defn moves [n x0 y0 x1 y1]
  (if (and (= x0 x1) (= y0 y1))
    []
    (let [[x y] (apply min-key
                       (fn [[cx cy]] (manhattan-dist cx cy x1 y1))
                       (neighbors n x0 y0))]
       (cons [x y] (moves n x y x1 y1)))))

(defn move-blank-to [n board num]
  (let [to      (get-idx board num)
        from    (get-idx board 0)
        [x0 y0] (get-pos n from)
        [x1 y1] (get-pos n to)
        m       (moves n x0 y0 x1 y1)]
    (map (fn [[x y]] (board (+ x (* n y)))) m)))


(defn solve [n board] [2 3 5 7])
