program expressionTest (input, output);

 var a, b : integer;
        c : real;
        x : array [0..5] of real;
        y : array [1..6] of integer;
 begin
   c := ((b + a)/ 2) div x[y[a]]
 end.
 
 { Output : 
     CODE
     1:  call main, 0
     2:  exit
     3:  PROCBEGIN main
     4:  alloc 30
     5:  add _0, _1, _15
     6:  move 2, _19
     7:  ltof _19, _16
     8:  ltof _15, _17
     9:  fdiv _16, _17, _18
     10:  move 1, _21
     11:  sub _1, _21, _20
     12:  load _9, _20, _22
     13:  move 0, _24
     14:  sub _22, _24, _23
     15:  load _3, _23, _25
     16:  ftol _25, _26
     17:  ftol _18, _27
     18:  div _26, _27, _28
     19:  ltof _28, _29
     20:  move _29, _2
     21:  free 30
     22:  PROCEND

 }