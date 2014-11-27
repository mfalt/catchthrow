function [u_k, x_k] = generatePaths()
phi = 0:0.01:1.5; dx = 2; dy = 1; l = .5; g= 9.82; T = 1.5;
endVel = (dx-l*cos(phi))*sqrt(g)./sqrt(2*cos(phi).*(dy*cos(phi)+dx*sin(phi)));
[Y,I] = min(endVel); phi = phi(I), endVel = endVel(I) %Choose min velocity
s.startx = [-.3  0  0  0]'; %The real values where the system starts
s.endx = [l endVel phi 0]';    %The desired final values
s.g = 9.82; s.m = 0.1; s.h=0.01;    %Define constants
s.Q=10*diag([0 .5 .1 .1]);  %Cost on state deviations (compared to s.endx)
s.R=.2;                    %Cost on control signal (derivative of beam)
s.QT=10*diag([10 5 2 1]); %Cost on final state deviation (from s.endx)

[u_k, x_k] = BallAndBeam(T,s);   %Find a good initial guess

s.QT = 10*s.QT;
[u_k, x_k] = BallAndBeam(T,s, u_k);   %Improve trajectory
s.QT = 10*s.QT;
[u_k, x_k] = BallAndBeam(T,s, u_k);   %Improve trajectory
s.QT = 10*s.QT;
[u_k, x_k] = BallAndBeam(T,s, u_k);   %Improve trajectory
savePath(s, x_k, 'throwPath');

%s.startx = [.3  0  0  0]'; %The real values where the system starts
%s.endx = [l .1 -pi/8 0]';    %The desired final values
%s.m = 1; T = .6; s.Q=.01*diag([0 1 10 1]); s.QT = 100*diag([3 1 1 .1]);
%[u_k, x_k] = BallAndBeam(T,s);   %Generate trajectory


end

function savePath(s, x_k, name)
h = s.h;
posRef = x_k(1,:);
velRef = x_k(2,:);
angleRef = x_k(3,:);
angleVelRef = x_k(4,:);
save(name, 'h', 'posRef', 'velRef', 'angleRef', 'angleVelRef');
end