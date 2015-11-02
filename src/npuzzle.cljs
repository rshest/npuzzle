(ns npuzzle)

;; ## Data structures
(defrecord Board [
  cells        ; list of cell numbers, row-major, 0 stands for blank
  n            ; width of the board
  m            ; height of the board
  blank-pos])  ; cached blank position, [x, y]

(defn dist2
  "Squared Euclidian distance between points `[x0 y0]` and `[x1 y1]`"
  [[x0 y0] [x1 y1]]
  (let [dx (- x0 x1)
        dy (- y0 y1)]
    (+ (* dx dx) (* dy dy))))

(defn neighbors?
  "Returns true if points `p0` and `p1` are neighboring
  (vertically or horizontally) on the board"
  [p0 p1]
  (= 1 (dist2 p0 p1)))

;; ## Board utilities
(defn valid-pos?
  "Returns true if `[x y]` is a position inside the `board` boundaries"
  [board [x y]]
  (and (>= x 0) (< x (board :n))
    (>= y 0) (< y (board :m))))

(defn pos->idx
  "Converts `[x y]` board position to the cell index (row-major)"
  [board [x y]]
  {:pre [(valid-pos? board [x y])]}
  (+ x (* (board :n) y)))

(defn idx->pos
  "Converts cell index `idx` (row major) to board position `[x y]`"
  [board idx]
  {:pre  [(>= idx 0) (< idx (* (board :n) (board :m)))]}
  [(mod idx (board :n)) (quot idx (board :n))])

(defn get-num-idx
  "Finds number's index in the board cell array"
  [board num]
  {:pre [(>= num 0) (<= num (* (board :n) (board :m)))]}
  (.indexOf (to-array (board :cells)) num))

(defn get-num-pos
  "Finds number's position in the board cell array"
  [board num]
  {:pre [(>= num 0) (<= num (* (board :n) (board :m)))]}
  (idx->pos board (get-num-idx board num)))

(defn get-num-at
  "Returns number at the board's position `pos`"
  [board pos]
  {:pre [(valid-pos? board pos)]}
  ((board :cells) (pos->idx board pos)))

(defn find-blank-pos
  "Returns the position of blank square on the `board`"
  [board]
  (get-num-pos board 0))

(defn swap
  "Returns a pair with swapped elements, `[y, x]`"
  [[x y]]
  [y x])

(defn transpose-board
  "Returns a board with swapped horizontal/vertical dimensions"
  [{m :m n :n blank-pos :blank-pos cells :cells}]
  {:blank-pos (swap blank-pos) :n m :m n
   :cells (->> cells
            (partition n)
            (apply mapv vector)
            (apply concat)
            vec)})

(defn make-board
  "Creates board from the array of `cells`
  (numbers, row-major, 0 is for blank), of width `n`"
  ([cells] (make-board cells (int (Math/sqrt (count cells)))))
  ([cells n]
    {:post [(= (* (% :n) (% :m)) (count cells))]}
    (let [board {:cells (vec cells)
                 :m     (quot (count cells) n)
                 :n     n}]
      (assoc board :blank-pos (find-blank-pos board)))))

(defn blank?
  "Returns true if position `pos` in the `board` contains a blank"
  [board pos]
  (and (valid-pos? board pos)
       (= 0 ((board :cells) (pos->idx board pos)))))

(defn move
  "Moves a number at `from-pos` on the `board` to the blank location
  (or, another way of seeing it, the blank moves to the `from-pos` location)"
  [board from-pos]
  {:pre [(neighbors? from-pos (board :blank-pos))]
   :post [(blank? % (% :blank-pos))
          (= (find-blank-pos %) (% :blank-pos))]}
  (let [to-idx    (pos->idx board (board :blank-pos))
        from-idx  (pos->idx board from-pos)
        cells     (board :cells)]
    (assoc board
      :cells (assoc cells
               from-idx (cells to-idx)
               to-idx   (cells from-idx))
      :blank-pos from-pos)))

;;  Directions, in which a piece can move (left, up, right, down)
(def dir-l [-1  0])
(def dir-u [ 0 -1])
(def dir-r [ 1  0])
(def dir-d [ 0  1])
(def directions [dir-l dir-u dir-r dir-d])

(defn neighbors
  "Returns list of valid neighbor positions on the `board` relative to `[x y]`"
  [board [x y]]
  (->> directions
    (map (fn [[dx dy]] [(+ x dx) (+ y dy)]))
    (filter (partial valid-pos? board))))

(defn num-solved?
  "Returns true if number at `idx` is at correct (solved) place on the `board"
  [{cells :cells} idx]
  {:pre [(>= idx 0) (< idx (count cells))]}
  (let [num (cells idx)]
    (= (inc idx)
      (if (zero? num) (count cells) num))))

(defn board-solved?
  "Returns true if the `board` is solved"
  [board]
  (every? (partial num-solved? board)
    (range 0 (count (board :cells)))))

(defn cells-valid?
  "Returns true if the `board` is a valid n-puzzle board"
  [cells]
  (let [nc (count cells)]
    (= (set (range 0 nc)) (set cells))))

(defn path
  "Finds a sequence of steps (positions) to get from position `p0` to position `p1`.
  `can-move?` is an optional predicate for the allowed cells.
  The path is expected to exist."
  ([board p0 p1] (path board p0 p1 (constantly true)))
  ([board p0 p1 can-move?]
    {:post [(or (nil? %) (seq? %))]}
    (loop [res      []
           cur-pos  p0]
      (if (= cur-pos p1) (seq res)
        (let [can-move'?  #(and (can-move? %)
                             (not (some #{%} res))
                             (not= % p0))
              neighbors'  (filter can-move'? (neighbors board cur-pos))
              new-pos     (apply min-key (partial dist2 p1) neighbors')]
          (recur (conj res new-pos) new-pos))))))

(defn eval-moves
  "For a sequence of `moves` (represented by positions of the blank)
  returns a sequence of evaluated boards, starting from the initial `board`"
  [board moves]
  (rest (reductions (fn [board' pos] (move board' pos))
          board moves)))

(defn move-num-middle
  "Finds a sequence of moves (blank positions) needed in order
  for the number at position `p0` to get to the position `p1`.
  `can-move?` is a predicate for the allowed cells.
  Assumes that `p1` is neither on the right nor the bottom edge"
  [board p0 p1 can-move?]
  (let [moves (path board p0 p1 can-move?) ; number path
        mfn   (fn [[res blank-pos last-pos] cur-pos]
                [(concat
                   res
                   (path board blank-pos cur-pos
                     #(and (not= % last-pos) (can-move? %)))
                   [last-pos])
                 last-pos cur-pos])
        acc   (reduce mfn [[] (board :blank-pos) p0] moves)]
    (first acc)))

(defn move-num-redge
  "Special case of moving a piece from [`x0`,`y0`] to [`x1`, `y1`] on right edge.
  `can-move?` is a move masking predicate."
  [board [x0 y0] [x1 y1] can-move?]
  {:pre [= x0 (dec (board :n))]}
  (let [below? (= y0 (inc y1))
        p0     [x0 (if below? (inc y0) y0)]
        m0     (if below? (move-num-middle board [x0 y0] p0 can-move?))
        board' (if below? (last (eval-moves board m0)) board)]
    (concat m0
      (move-num-middle board' [(dec x1) y1] [x1 y1] can-move?)
      (move-num-middle (assoc board' :blank-pos [(dec x1) y1])
        p0 [x1 (inc y1)] can-move?)
      [[(dec x1) (+ 2 y1)] [(dec x1) (inc y1)]
       [(dec x1) y1] [x1 y1] [x1 (inc y1)]])))

(defn move-num-bedge
  "Same as `move-num-redge, but for the bottom edge`"
  [board p0 p1 can-move?]
  {:pre [= (second p0) (dec (board :m))]}
  (let [board'  (transpose-board board)
        moves   (move-num-redge board' (swap p0) (swap p1)
                  (comp can-move? swap))]
    (map swap moves)))

(defn move-num
  "Moves correct number on a `board` to the target position `[x, y]`"
  [board [x y] can-move?]
  (let [idx    (pos->idx board [x y])
        [n m]  [(board :n) (board :m)]
        p0     (get-num-pos board (inc idx))
        movefn (cond
                 (= p0 [x y])  (constantly [])
                 (and (= (inc x) n) (< y (- m 2))) move-num-redge
                 (and (= (inc y) m) (< x (- n 2))) move-num-bedge
                 :else         move-num-middle)]
    (movefn board p0 [x y] can-move?)))

(defn solve-order
  "Returns sequence of positions to solve the board `n` by `m`"
  ([n]
    (apply concat
      (for [k (range 0 (- n 1))]
        (concat
          (map #(vector % k) (range k n))
          (map #(vector k %) (range (inc k) n))))))
  ([n m]
    {:pre [(> n 1) (> m 1)]}
    (let [i0 (max 0 (- n m))
          j0 (max 0 (- m n))]
      (concat
        (for [i (range 0 i0) j (range 0 m)] [i j])
        (for [j (range 0 j0) i (range 0 n)] [i j])
        (map (fn [[i j]] [(+ i i0) (+ j j0)])
          (solve-order (min n m)))))))

(defn top-left-edge?
  "Returns true if position `[x y]` is in the masked left-top area,
  (according to the board filling rule) relative to the pivot position `[px py]`.
  The board is assumed to be square."
  [[px py] [x y]]
  (if (> py px)
    (or (< x px) (<= y px) (and (= x px) (< y py)))
    (or (< y py) (< x py) (and (= y py) (< x px)))))

(defn solve
  "Returns sequence of moves (positions) needed in order to solve given `board`
  using a greedy algorithm."
  [board]
  (let [[n m] [(board :n) (board :m)]
        i0    (max 0 (- n m))
        j0    (max 0 (- m n))
        order (solve-order n m)]
    (second (reduce
      (fn [[board' res] [x y]]
        (let [can-move? (fn [[i j]]
                          (not (top-left-edge?
                                 [(- x i0) (- y j0)]
                                 [(- i i0) (- j j0)])))
              moves     (move-num board' [x y] can-move?)
              board''   (if (empty? moves) board'
                          (last (eval-moves board' moves)))]
          [board'' (concat res moves)]))
      [board []]
      order))))

(defn solve-nums
  "Same as `solve`, but returns a sequence of pieces' numbers"
  [board]
  (let [moves  (solve board)
        boards (eval-moves board moves)]
    (map get-num-at (conj boards board) moves)))
