program expressionTest (input, output);

 var a, b : integer;
        c : real;
        x : array [0..5] of real;
 begin
   b := a - 4;
   c := ((b + a)/ 2) div x[a]
 end.
 
{ Output:

CODE
1:  call main, 0
2:  exit
3:  PROCBEGIN main
4:  alloc 23
5:  move 4, _10
6:  sub _1, _10, _9
7:  move _9, _0
8:  add _0, _1, _11
9:  move 2, _15
10:  ltof _15, _12
11:  ltof _11, _13
12:  fdiv _12, _13, _14
13:  move 0, _17
14:  sub _1, _17, _16
15:  load _3, _16, _18
16:  ftol _18, _19
17:  ftol _14, _20
18:  div _19, _20, _21
19:  ltof _21, _22
20:  move _22, _2
21:  free 23
22:  PROCEND
}