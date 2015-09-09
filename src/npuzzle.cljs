(ns npuzzle
  (:require [tailrecursion.priority-map
             :refer [priority-map priority-map-by]]))

;; ## Constants
;;  Directions, in which a piece can move (left, up, right, down)
(def DIR_L [-1  0])
(def DIR_U [ 0 -1])
(def DIR_R [ 1  0])
(def DIR_D [ 0  1])
(def DIRECTIONS [DIR_L DIR_U DIR_R DIR_D])

;; ![alt text](board.png)
(defn move [board from to]
  (assoc board
    from (board to)
    to (board from)))

(defn get-idx [board num] (.indexOf (to-array board) num))
(defn get-pos [n idx] [(mod idx n) (quot idx n)])
(defn get-num [n board [x y]] (board (+ x (* n y))))
(defn get-num-pos [n board num] (get-pos n (get-idx board num)))
(defn get-blank-pos [n board] (get-num-pos n board 0))

(defn manhattan-dist [[x0 y0] [x1 y1]]
  (+ (Math/abs (- x0 x1)) (Math/abs (- y0 y1))))

(defn neighbor? [p0 p1]
  (= 1 (manhattan-dist p0 p1)))

(defn valid-cell? [n [x y]]
  (and (>= x 0) (< x n) (>= y 0) (< y n)))

(defn neighbors [n [x y]]
  (->> DIRECTIONS
       (map (fn [[dx dy]] [(+ x dx) (+ y dy)]))
       (filter (partial valid-cell? n))))

(defn moves
  "Finds a sequence of moves to get from position `p0` to position `p1`.
  If `can-move` is provided, it is used to mask out prohibited directions
  "
  ([n p0 p1] (moves n p0 p1 (constantly true)))
  ([n p0 p1 can-move]
    (if (= p0 p1) []
      (let [p (apply min-key (partial manhattan-dist p1)
                         (filter can-move (neighbors n p0)))]
         (cons p (moves n p p1))))))

(defn move-blank-to [n board p1]
  (let [p0 (get-blank-pos n board)
        m  (moves n p0 p1)]
    (map (partial get-num n board) m)))

(defn move-piece-to [n board pos piece]
  (move-blank-to n board pos))


(defn solve [n board] [2 3 5 7])
