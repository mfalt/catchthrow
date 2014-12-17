%% Initialization
est_JB;
m = 0.1; % Do not know best mass to linearize around
x1 = 0; % Ball position
x0 = [x1; 0; 0; 0; m; 0]; % Last state linearized around u according to report..?
h = 0.01;

A = [0 1 0 0 0 0;
    0 0 -5*g/7 0 0 0;
    0 0 0 1 0 0;
    m*g/JB 0 kB/JB 0 g*x1/JB 0;
    0 0 0 0 0 0;
    1 0 0 0 0 0];
B = [0; 0; 0; ku/JB; 0; 0];
C = [1 0 0 0 0 0;
    0 0 1 0 0 0];
D = [0; 0];

%% Discretize system
contsys = ss(A, B, C, D);
discsys = c2d(contsys, h);
Phi = discsys.a;
Gamma = discsys.b;

%% Solve riccati equations
[??] = dare(???);
L = ??;

%% Store system
save timeInvLQG.mat Phi Gamma C D L K x0;




