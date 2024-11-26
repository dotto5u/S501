<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Attribute\Route;
use App\Repository\ImageRepository;

class ApiController extends AbstractController
{
    #[Route('/images/all', name: 'app_images_all', methods: ['GET'])]
    public function all(ImageRepository $imageRepository): JsonResponse
    {
        $images = $imageRepository->getAll();

        if (empty($images)) {
            return new JsonResponse([]);
        }

        $json = array_map(function ($image) {
            return [
                'id' => $image->getId(),
                'url' => $image->getUrl(),
                'categories' => array_map(function ($category) {
                    return [
                        'id' => $category->getId(),
                        'label' => $category->getLabel(),
                    ];
                }, $image->getCategories()->toArray()),
            ];
        }, $images);

        return new JsonResponse($json);
    }
}
