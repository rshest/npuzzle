(ns npuzzle
  (:require [tailrecursion.priority-map
             :refer [priority-map priority-map-by]]))

;; ## Constants
;;  Directions, in which a piece can move (left, up, right, down)
(def dir-l [-1  0])
(def dir-u [ 0 -1])
(def dir-r [ 1  0])
(def dir-d [ 0  1])
(def directions [dir-l dir-u dir-r dir-d])

;; ![alt text](board.png)
(defn move [board from to]
  (assoc board
    from (board to)
    to (board from)))

(defn get-idx
  [board num]
  (.indexOf (to-array board) num))

(defn get-pos
  [n idx]
  [(mod idx n) (quot idx n)])

(defn pos->idx
  [n [x y]]
  (+ x (* n y)))

(defn get-num
  [n board p]
  (board (pos->idx n p)))

(defn get-num-pos
  [n board num]
  (get-pos n (get-idx board num)))

(defn get-blank-pos
  [n board]
  (get-num-pos n board 0))

(defn dist2
  "
  "
  [[x0 y0] [x1 y1]]
  (let [dx (- x0 x1)
        dy (- y0 y1)]
    (+ (* dx dx) (* dy dy))))

(defn manhattan-dist
  "
  "
  [[x0 y0] [x1 y1]]
  (+ (Math/abs (- x0 x1)) (Math/abs (- y0 y1))))

(defn neighbor?
  [p0 p1]
  (= 1 (manhattan-dist p0 p1)))

(defn valid-cell?
  [n [x y]]
  (and (>= x 0) (< x n) (>= y 0) (< y n)))

(defn top-left-edge?
  "Returns true if position [`x`, `y`] is in the masked left-top area,
  (according to the board filling rule) relative to the `pivot` position"
  [pivot [x y]]
  (let [[px py] pivot
        vert?   (> py px)]
    (if vert?
      (or (< x px) (<= y px) (and (= x px) (< y py)))
      (or (< y py) (<  x py) (and (= y py) (< x px))))))

(defn neighbors
  [n [x y]]
  (->> directions
       (map (fn [[dx dy]] [(+ x dx) (+ y dy)]))
       (filter (partial valid-cell? n))))

(defn moves
  "Finds a sequence of moves to get from position `p0` to position `p1`.
  `can-move` is an optional predicate for the allowed cells
  "
  ([n p0 p1] (moves n p0 p1 (constantly true)))
  ([n p0 p1 can-move]
   (loop [res []
          pc p0]
     (if (or (= pc p1) (> (count res) 10)) res
      (let [cm #(and (can-move %) (not (some #{%} res)) (not= % p0))
            nb (filter cm (neighbors n pc))
            p  (apply min-key (partial dist2 p1) nb)]
        (recur (conj res p) p))))))

(defn move-blank-to
  [n board p1]
  (let [p0 (get-blank-pos n board)
        m  (moves n p0 p1)]
    (map (partial get-num n board) m)))

(defn eval-moves
  "Evaluates a sequence of blank `moves`, represented by coordinates
  and starting from `blank-pos`.
  Returns a triple of [newBoard newBlankPos numbers]
  "
  [n board moves blank-pos]
  (reduce (fn [[cboard bp res] m]
            [(move cboard (pos->idx n bp) (pos->idx n m))
             m
             (conj res (get-num n cboard m))])
          [board blank-pos []] moves))

(defn move-piece-to
  [n board p1 piece]
  (let [pb      (get-blank-pos n board)
        cmove   #(not (top-left-edge? p1 %))
        p0      (get-num-pos n board piece)
        pmoves  (vec (moves n p0 p1 cmove))
        mfn     (fn [[bpos cpos res] p]
                  [cpos p
                   (concat res
                           (moves n bpos p
                                  #(and (not= % cpos) (cmove %))) [cpos])])
        [_ _ m] (reduce mfn [pb p0 []] pmoves)]
    (eval-moves n board m pb)))

(defn move-piece
  [n board p1 piece]
  (let [[_ _ m] (move-piece-to n board p1 piece)] m))

(defn move-piece-redge
  "Special case of moving a piece into a position `p1` on right edge"
  [n board [x y] piece]
  (let
    [[b1 h1 m1] (move-piece-to n board [x y] (dec piece))
     [b2 h2 m2] (eval-moves n b1 [[(dec x) (dec y)]] h1)
     [b3 h3 m3] (move-piece-to n b2 [x (inc y)] piece)
     [_  _  m4] (eval-moves n b3 (moves n h3 [(dec x) y]) h3)]
  (concat m1 m2 m3 m4 [(dec piece) piece])))

(defn solve
  [n board]
  [2 3 5 7])
