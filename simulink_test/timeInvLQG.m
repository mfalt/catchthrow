%% Initialization
est_JB;

kB = 0; %Temporarily!!!!!!!

m = 0.1; % Do not know best mass to linearize around
x1 = 0; % Ball position
x0 = [x1; 0; 0; 0; m; 0]; % Last state linearized around u according to report..?
n = length(x0);
h = 0.01;

A = [0 1 0 0 0 0;
    0 0 -5*g/7 0 0 0;
    0 0 0 1 0 0;
    -m*g/JB 0 kB/JB 0 -g*x1/JB 0;
    0 0 0 0 0 0;
    1 0 0 0 0 0];

% B = [0; 0; 0; ku/JB; 0; 0];
B = [0 0; 0 0; 0 0; ku/JB 0; 0 1; 0 0]; %u2 controls mass

% C = [1 0 0 0 0 0;
%     0 0 1 0 0 0];
% C = [1 0 0 0 0 0;
%     0 0 1 0 0 0;
%     0 0 0 0 0 1]; % y3 observes integral pos
C = [1 0 0 0 0 0;
    0 0 1 0 0 0;
    0 0 0 0 1 0; % y3 observes mass
    0 0 0 0 0 1]; % y4 observes integral pos

D = zeros(size(C,1), size(B,2));

u0 = -A(4,:)*x0 ./ B(4,1);

%% Discretize system
contsys = ss(A, B, C, D);
discsys = c2d(contsys, h);
Phi = discsys.a;
Gamma = discsys.b;

%% Solve riccati equations
Q = diag([1111,10000,816,13131,0,2500]);
% R = 1;
% N = zeros(n,1);
R = diag([1e4 1e10]);
N = zeros(n,length(R));
% [~,~,L] = dare(Phi,Gamma,Q,R);

% L = lqr(discsys, Q, R, N) % Minimizes J = Sum {x'Qx + u'Ru + 2*x'Nu}
L = dlqr(Phi, Gamma, Q, R, N) % Minimizes J = Sum {x'Qx + u'Ru + 2*x'Nu}

Qn =diag([0 1 0 1 1 0]);
%Qn =diag([1 1 1 1 1 1])*1e5;
% Qn = eye(n);
Rn = diag([1 1 1e10 1e10]);
Nn = zeros(n,length(Rn));
% kalman_artif_sys = ss(discsys.a', discsys.c', zeros(1,n), zeros(1,size(discsys.c,1)), h);
% K = lqr(kalman_artif_sys, Qn, Rn, Nn)';
K = dlqr(Phi', C', Qn, Rn, Nn)';

%% Remove u2
L = L(1,:)
B = B(:,1)*0;
Gamma = Gamma(:,1);
D = D(1:2,1);
C = C(1:2,:);
K = K(:,1:2)


%% Store system
save timeInvLQG.mat Phi Gamma C D L K x0 u0;




