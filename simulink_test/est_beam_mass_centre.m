%%
angleData = angle.Data;
uData = u.Data;

%%
N = find(angleData >= -min(angleData), 1);
% N = min(length(angleData), length(uData));
angleData = angleData(1:N);
uData = uData(1:N);

%%
par = regress(uData, angleData); % par = -k_b/k_u. u=par*ang
% parpoly = polyfit(angleData, uData, 5);
