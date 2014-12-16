%%
load JB_data;
tphi = angle.Time;
phi = angle.Data;
tu = u.Time;
uval = u.Data;

% figure;
% plot(phi);
% 
% figure;
% plot(uval);

phiacc = filter([-1 2 -1], 1, phi) ./ tphi.^2;
% phiacc = diff(phi) ./ tphi(2:end);

tphi = tphi(2:end-1);
phi = phi(2:end-1);
tu = tu(2:end-1);
uval = uval(2:end-1);
phiacc = phiacc(2:end-1);

%%
acc_idx = abs(phiacc) > 
tphi = tphi(2:end-1);
phi = phi(2:end-1);
tu = tu(2:end-1);
uval = uval(2:end-1);
phiacc = phiacc(2:end-1);

% Do not forget scaling on phi





