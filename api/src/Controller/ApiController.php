<?php

namespace App\Controller;

use App\Entity\Category;
use App\Entity\Image;
use App\Repository\CategoryRepository;
use App\Repository\ImageRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;

class ApiController extends AbstractController
{
    #[Route('/health', name: 'app_health', methods: ['GET'])]
    public function health(): JsonResponse
    {
        $json = ['Symfony API is currently working'];

        return new JsonResponse($json);
    }

    #[Route('/image/all', name: 'app_image_all', methods: ['GET'])]
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

    #[Route('/image/upload', name: 'app_image_upload', methods: ['POST'])]
    public function upload(Request $request, EntityManagerInterface $entityManager, CategoryRepository $categoryRepository): JsonResponse {
        $uploadedFile = $request->files->get('image');

        if (!$uploadedFile || !$uploadedFile->isValid()) {
            return new JsonResponse(['error' => 'Invalid file upload'], 400);
        }

        $categoriesData = $request->request->get('categories');
        if (!$categoriesData) {
            return new JsonResponse(['error' => 'Categories are required'], 400);
        }

        $categoriesArray = json_decode($categoriesData, true);
        if (!is_array($categoriesArray)) {
            return new JsonResponse(['error' => 'Invalid categories format'], 400);
        }

        $categories = [];
        foreach ($categoriesArray as $categoryData) {
            if (!isset($categoryData['label'])) {
                continue;
            }

            $label = $categoryData['label'];
            $category = $categoryRepository->findOneBy(['label' => $label]);

            if (!$category) {
                $category = new Category();
                $category->setLabel($label);
                $entityManager->persist($category);
            }
            $categories[] = $category;
        }

        $uploadDir = $this->getParameter('kernel.project_dir').'/public/uploads';
        $newFilename = uniqid().'.'.$uploadedFile->guessExtension();
        try {
            $uploadedFile->move($uploadDir, $newFilename);
        } catch (FileException) {
            return new JsonResponse(['error' => 'Failed to upload file'], 500);
        }

        $image = new Image();
        $image->setUrl('/uploads/'.$newFilename);
        foreach ($categories as $category) {
            $image->addCategory($category);
        }

        $entityManager->persist($image);
        $entityManager->flush();

        return new JsonResponse(['success' => 'Image uploaded successfully']);
    }
}
