%ima1=double(imread('image_1.3or93zbi.000000a.jpg'));
%ima1=double(imread('Rota_Iso_D4_C4_N0_OUT0_LT0_0010_A.tif'));
%ima1=ima1(:,:,1);
var=velocities16x16;
iaWidth=double(var.iaWidth);
iaHeight=double(var.iaHeight);
iaIncrWidth=iaWidth*double(parameters.overlapFactor);
iaIncrHeight=iaHeight*double(parameters.overlapFactor);
top=double(var.margins.top);
left=double(var.margins.left);

u=double(var.u(:,:,1));
v=double(var.v(:,:,1));

[N,histU]=hist(reshape(u, size(u,1)*size(u,2),1), 30);
total = sum(N);
contained = 0;
startIdx=0;
endIdx=0;
for i = 1 : length(N)
    contained = contained + N(i);
    if contained / total >= 0.05
        startIdx = i - 1;        
        break;
    end
end
startIdx = max(startIdx, 1);
contained = 0;
for i = startIdx : length(N)
    contained = contained + N(i);
    if contained / total >= 0.95
        endIdx = i;
        break;
    end
end

u=min(max(histU(startIdx), u), histU(endIdx));

[N,histV]=hist(reshape(v, size(v,1)*size(v,2),1), 30);
total = sum(N);
contained = 0;
startIdx=0;
endIdx=0;
for i = 1 : length(N)
    contained = contained + N(i);
    if contained / total >= 0.05
        startIdx = i - 1;        
        break;
    end
end
startIdx = max(startIdx, 1);
contained = 0;
for i = startIdx : length(N)
    contained = contained + N(i);
    if contained / total >= 0.95
        endIdx = i;
        break;
    end
end

v=min(max(histV(startIdx), v), histV(endIdx));
norm=sqrt(v.^2+u.^2);
minScale=min(min(norm));
maxScale=max(max(norm));

x=(0:size(u,2)-1)*iaIncrWidth+iaWidth/2+left;
y=(0:size(u,1)-1)*iaIncrHeight+iaHeight/2+top;
[X,Y]=meshgrid(double(parameters.imageWidth) - x,double(parameters.imageHeight) - y);
%figure();
%hold on; pcolor(ima1), shading flat, colormap gray;
figure();
hold on; pcolor(X, Y, norm), shading flat, colormap('jet'), caxis([minScale maxScale]), colorbar;
quiver(X, Y, v, u, 'k');
