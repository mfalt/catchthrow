%%
load JB_data;
tphi = angle.Time;
phisc = angle.Data;
tu = u.Time;
uval = u.Data;

n = 10; % Level of smoothing

% figure;
% plot(phi);
% 
% figure;
% plot(uval);

hphi = diff(tphi);
hphi = hphi(1:end-1);
h2phi = diff(diff(tphi));
tphi = tphi(2:end-1);
phisc = phisc(2:end-1);
tu = tu(2:end-1);
uval = uval(2:end-1);

filt = -smooth_diff(n);
phiscvel = filter(filt, 1, phisc) ./ hphi;
phiscacc = filter(conv(filt, filt), 1, phisc) ./ hphi.^2;

tphi = tphi(n+1:end-n);
phisc = phisc(n+1:end-n);
tu = tu(n+1:end-n);
uval = uval(n+1:end-n);
phiscacc = phiscacc(n+1:end-n);
phiscvel = phiscvel(n+1:end-n);

%%
acc_idx = abs(phiscacc) > 10;
tphi = tphi(acc_idx);
phisc = phisc(acc_idx);
tu = tu(acc_idx);
uval = uval(acc_idx);
phiscacc = phiscacc(acc_idx);

%%
m_weigh = 0.172;
g = 9.82;
x_weigh = 0.55;
u_weigh = 3;

k_phisc = 40/pi;
kB_ku = -0.24*k_phisc;

ku = m_weigh*g*x_weigh / u_weigh
kB = kB_ku * ku

%%
JB_samples = (k_phisc*ku*uval + kB*phisc) ./ phiscacc;
JB = mean(JB_samples)




