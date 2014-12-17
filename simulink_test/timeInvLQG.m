%% Initialization
est_JB;
m = 0.1; % Do not know best mass to linearize around
x1 = 0; % Ball position
x0 = [x1; 0; 0; 0; m; 0]; % Last state linearized around u according to report..?
n = length(x0);
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
% [~,~,L] = dare(Phi,Gamma,Q,R);

Q = diag([1111,10000,816,13131,0,25]);
R = 1;
N = zeros(n,1);
eig([Q N;N' R])
L = lqr(discsys, Q, R, N) % Minimizes J = Sum {x'Qx + u'Ru + 2*x'Nu}

Qn = eye(6);
Rn = eye(2);
Nn = zeros(n,2);
[~,K,~] = kalman(discsys, Qn, Rn, Nn); % Qn state noise var, Rn measurement noise var, Nn cross terms.

%% Store system
save timeInvLQG.mat Phi Gamma C D L K x0;




